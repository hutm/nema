function [totalScore,aMask]=seqAssignEval(refDesc,resultDesc,doUniMapping,borderLocs)
%
% [totalScore,aMask]=seqAssignEval(refDesc,resultDesc,doUniMapping,borderLocs)
%
% Based on
%  Peeters, "Sequence representation of music structure using higher-order
%  similarity matrix and maximum-likelihood approach", ISMIR2007
%
% Injective mapping requirement added later.

% Jouni Paulus, jouni.paulus@tut.fi, 2.9.2009

if (~exist('doUniMapping','var') || isempty(doUniMapping))
  doUniMapping=1;
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

if (doUniMapping)
  [aMask,bestCost]=findUniqueMapping(confMat);
else
  % each reference label is assigned to the found with the highest coincidence
  aMask=zeros(refLabelCount,resultLabelCount);
  for (refIdx=1:refLabelCount)
    [ignore,maxIdx]=max(confMat(refIdx,:));
    aMask(refIdx,maxIdx)=1;
  end;
end;

totalScore=sum(sum(aMask.*confMat))/sum(sum(confMat));

