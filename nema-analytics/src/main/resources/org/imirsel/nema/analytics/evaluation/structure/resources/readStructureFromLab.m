function fileInfo=readStructureFromLab(labFile)
%
% fileInfo=readStructureFromLabM(labFile)
%
% Routine to read a structure description from .lab file.
% 

% Jouni Paulus, jouni.paulus@tut.fi, 25.4.2008
% cut out some of the fat, 12.10.2009

fId=fopen(labFile,'r');
if (fId==-1)
  fileInfo=[];
  return
end;

labelSet={};
labelTimes={};

% each line is formed by:
% [startTime stopTime label]
while (1)
  fileLine=fgetl(fId);
  if (fileLine==-1)
    break;
  end;
  
  fileLine=trimB(fileLine);
  
  if (length(fileLine)<1 || strcmpi(fileLine(1),'#'))
    % empty line, or comment
    continue;
  end;
  
  [startTtxt,fileLine]=strtok(fileLine);
  [endTtxt,fileLine]=strtok(fileLine);
  partTag=trimB(fileLine);
  
  startT=str2double(startTtxt);
  endT=str2double(endTtxt);
  
  % check if we have met this tag already
  tmpIdx=find(ismember(labelSet,partTag));
  if (isempty(tmpIdx))
    % new tag
    labelSet{end+1,1}=partTag;
    labelTimes{end+1,1}=[startT endT];
  else
    labelTimes{tmpIdx}=[labelTimes{tmpIdx} startT endT];
  end;    
end;
fclose(fId);

fileInfo.labels=labelSet;
fileInfo.times=labelTimes;

function outStr=trimB(inStr)
%
% Apply deblank on both ends of the string
%
outStr=deblank(inStr(end:-1:1));
outStr=deblank(outStr(end:-1:1));