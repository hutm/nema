function [overSegScore,underSegScore,entResGivenRef,entRefGivenRes]=condEntropyEval(refDesc,resultDesc,borderLocs,excludeList)
%
% [overSegScore,underSegScore,entResGivenRef,entRefGivenRes]=condEntropyEval(refDesc,resultDesc,borderLocs,excludeList)
%
% Structure analysis description evaluation based on conditional entropy.
%
% Based on
%  H. Lukashevich, "Towards quantitative measures of evaluating song 
%  segmentation", ISMIR 2008, pp.375-380

% Jouni Paulus, jouni.paulus@tut.fi, 29.9.2008
% removed the "padding" label used to align the descriptions form the 
% evaluation as it caused problems, 17.10.2009, J.P.

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

% added 20091017: remove the "padding" label
confMat(refLabelCount,:)=[];
confMat(:,resultLabelCount)=[];
refLabelCount=refLabelCount-1;
resultLabelCount=resultLabelCount-1;

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

% p_{ij}
jointDistribution=confMat/sum(sum(confMat));
% p_i^a
refMarginal=sum(confMat,2)/sum(sum(confMat));
% p_i^e
resMarginal=sum(confMat,1)/sum(sum(confMat));

% p_{ij}^{a|e}
pRefGivenResult=confMat./repmat(sum(confMat,1),refLabelCount,1);
% p_{ji}^{e|a}
pResultGivenRef=confMat./repmat(sum(confMat,2),1,resultLabelCount);

pRefGivenResult(~isfinite(pRefGivenResult))=0;
pResultGivenRef(~isfinite(pResultGivenRef))=0;

% H(E|A)
tmpEnt=pResultGivenRef.*log2(pResultGivenRef);
tmpEnt(~isfinite(tmpEnt))=0;
entResGivenRef=-sum(refMarginal.*sum(tmpEnt,2));
% H(A|E)
tmpEnt=pRefGivenResult.*log2(pRefGivenResult);
tmpEnt(~isfinite(tmpEnt))=0;
entRefGivenRes=-sum(resMarginal.*sum(tmpEnt,1));

% H(E|A)_{max}
maxEntResGivenRef=log2(resultLabelCount);
% H(A|E)_{max}
maxEntRefGivenRes=log2(refLabelCount);

% S_o
if (resultLabelCount<=1)
  if (entResGivenRef>eps)
    overSegScore=0;
  else
    overSegScore=1;
  end;
else
  overSegScore=1-entResGivenRef/maxEntResGivenRef;
end;

% S_u
if (refLabelCount<=1)
  if (entRefGivenRes>eps)
    underSegScore=0;
  else
    underSegScore=1;
  end;
else
  underSegScore=1-entRefGivenRes/maxEntRefGivenRes;
end;

%keyboard;

