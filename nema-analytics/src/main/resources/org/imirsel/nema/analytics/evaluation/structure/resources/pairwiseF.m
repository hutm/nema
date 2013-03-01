function [pwF,pwPrecision,pwRecall]=pairwiseF(refDesc,resultDesc,borderLocs,isFrames,doIncludeSelf)
%
% [pwF,pwPrecision,pwRecall]=pairwiseF(refDesc,resultDesc,borderLocs,isFrames,doIncludeSelf)
%
% Clustering based performance measure as proposed in
% Levy, M. and Sandler, M. "Structural segmentation of musical audio by
%  constrained clustering", IEEE TSALP 16(2), 2008 

% Jouni Paulus, jouni.paulus@tut.fi, 23.1.2008

if (~exist('isFrames','var') || isempty(isFrames) || length(isFrames)<2)
  isFrames=[0 0];
end;
if (isstruct(refDesc))
  refDesc=refDesc.times;
end;
if (isstruct(resultDesc))
  resultDesc=resultDesc.times;
end;
if (~exist('doIncludeSelf','var') || isempty(doIncludeSelf))
  doIncludeSelf=0;
end;

if (~exist('borderLocs','var') || isempty(borderLocs))
   % generate artificial borders
  minTime=realmax;
  maxTime=realmin;

  for (lIdx=1:length(refDesc))
    minTime=min(min(minTime,refDesc{lIdx}(:)));
    maxTime=max(max(maxTime,refDesc{lIdx}(:)));
  end;
  for (lIdx=1:length(resultDesc))
    minTime=min(min(minTime,resultDesc{lIdx}(:)));
    maxTime=max(max(maxTime,resultDesc{lIdx}(:)));
  end;

  borderLocs=linspace(minTime,maxTime,500);  
end;

frameCount=length(borderLocs)-1;

% tranform into sequential representation
refSeq=desc2seq(refDesc,borderLocs,isFrames(1));
resultSeq=desc2seq(resultDesc,borderLocs,isFrames(2));

% calculate binary recurrence matrices
refRecMat=repmat(refSeq,1,frameCount)==repmat(refSeq',frameCount,1);
resultRecMat=repmat(resultSeq,1,frameCount)==repmat(resultSeq',frameCount,1);

% number of hits, references and results as number of frame pairs correctly
% assigned to same group
hitCount=sum(sum(triu(refRecMat.*resultRecMat.*(refRecMat==resultRecMat),1-doIncludeSelf)));
refCount=sum(sum(triu(refRecMat,1-doIncludeSelf)));
resultCount=sum(sum(triu(resultRecMat,1-doIncludeSelf)));

if (hitCount<eps)
  pwPrecision=0;
  pwRecall=0;
  pwF=0;
else
  pwPrecision=hitCount/resultCount;
  pwRecall=hitCount/refCount;
  pwF=2*pwPrecision*pwRecall/(pwPrecision+pwRecall);
end;




