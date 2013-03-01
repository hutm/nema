function outSeq=desc2seq(inDesc,borderLocs,isFrame)
%
% outSeq=desc2seq(inDesc,borderLocs,isFrame)
%
% Separated from pairwiseF.m

% Jouni Paulus, jouni.paulus@tut.fi, 18.2.2008

groupCount=length(inDesc);
frameCount=length(borderLocs)-1;
outSeq=zeros(frameCount,1);

for (gIdx=1:groupCount)
  thisTimes=inDesc{gIdx};
  
  if (size(thisTimes,1)>1)
    % input times are in [s1 e1;s2 e2...] format. transfrom to [s1 e1 s2 e1...]
    thisTimes=thisTimes';
    thisTimes=thisTimes(:)';
  end;
  
  if (~isFrame)
    % transform to frames
    for (tIdx=1:length(thisTimes))
      % assign to closest border
      [minDev,minIdx]=min(abs(borderLocs-thisTimes(tIdx)));
      thisTimes(tIdx)=minIdx;
    end;
  end;

  occCount=length(thisTimes)/2;
  for (oIdx=1:occCount)
    outSeq(thisTimes((oIdx-1)*2+1):(thisTimes((oIdx-1)*2+2)-1))=gIdx;
  end;
end;
