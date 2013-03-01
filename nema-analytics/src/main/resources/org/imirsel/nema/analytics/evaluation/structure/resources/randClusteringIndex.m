function [R,aVal,bVal,cVal,dVal]=randClusteringIndex(refDesc,resultDesc,borderLocs,excludeList)
%
% [R,aVal,bVal,cVal,dVal]=randClusteringIndex(refDesc,resultDesc,borderLocs,excludeList)
%
% Based on
%  Hubert & Arabie, "Comparing partitions", Journal of Classification, 1985

% Jouni Paulus, jouni.paulus@tut.fi, 4.6.2009

if (~exist('excludeList','var'))
  excludeList={'sil','silece','silence'};
end;
if (isempty(excludeList) && ~iscell(excludeList))
  excludeList={[]};
end;

if (~exist('borderLocs','var') || isempty(borderLocs))
  % generate artificial borders
  borderCount=1000;
  minTime=realmax;
  maxTime=realmin;
  
  for (lIdx=1:length(refDesc.times))
    minTime=min(min(minTime,refDesc.times{lIdx}(:)));
    maxTime=max(max(maxTime,refDesc.times{lIdx}(:)));
  end;
  for (lIdx=1:length(resultDesc.times))
    minTime=min(min(minTime,resultDesc.times{lIdx}(:)));
    maxTime=max(max(maxTime,resultDesc.times{lIdx}(:)));
  end;
  
  borderLocs=linspace(minTime,maxTime,borderCount);
end;

isFrames=zeros(1,2);

frameCount=length(borderLocs)-1;

% transform into sequential representation
refSeq=desc2seq(refDesc.times,borderLocs,isFrames(1));
resultSeq=desc2seq(resultDesc.times,borderLocs,isFrames(2));

refLabels=lower([refDesc.labels(:); '-']);
resultLabels=lower([resultDesc.labels(:); '-']);

refLabelCount=length(refLabels);
resultLabelCount=length(resultLabels);

refSeq(refSeq==0)=refLabelCount;
resultSeq(resultSeq==0)=resultLabelCount;

% framewise confusion matrix for labels
confMat=zeros(refLabelCount,resultLabelCount);
for (fIdx=1:frameCount)
  confMat(refSeq(fIdx),resultSeq(fIdx))=confMat(refSeq(fIdx),resultSeq(fIdx))+1;  
end;

% check the label correspondence
labelMask=zeros(refLabelCount,resultLabelCount);
excludeMask=ones(refLabelCount,resultLabelCount);
for (refLabelIdx=1:refLabelCount)
  resIdx=find(ismember(resultLabels,refLabels{refLabelIdx}));
  
  if (~isempty(resIdx))
    labelMask(refLabelIdx,resIdx)=1;
  end;
  
  % check for exclude in reference
  if (ismember(refLabels{refLabelIdx},excludeList))
    excludeMask(refLabelIdx,:)=0;
  end;
end;

% the individual component terms

% #(element pairs in same clusters in both sets)
aVal=sum(sum(confMat.*(confMat-1)))/2;
% #(element pairs in different clusters in both sets)
bVal=(frameCount^2+sum(sum(confMat.^2)) - sum(sum(confMat,2).^2) - sum(sum(confMat,1).^2))/2;
% #(element pairs in same cluster in first set and different in second)
cVal=(sum(sum(confMat,1).^2) - sum(sum(confMat.^2)))/2;
% #(element pairs in different cluster in first set and same in second)
dVal=(sum(sum(confMat,2).^2) - sum(sum(confMat.^2)))/2;

R=(aVal+bVal)/(frameCount*(frameCount-1)/2);



