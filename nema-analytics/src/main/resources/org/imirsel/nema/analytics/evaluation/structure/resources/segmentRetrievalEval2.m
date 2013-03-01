function [Fmeasures,precRates,recRates,medianTrue2claim,medianClaim2true]=segmentRetrievalEval2(refStructure,resStructure,deltaT,excludeList)
%
% [Fmeasures,precRates,recRates,medianTrue2claim,medianClaim2true]=segmentRetrievalEval2(refStructure,resStructure,deltaT,excludeList)
%
% Segment boundary retrieval accuracy with allowed deviations (in deltaT)
%   boundary retrieval F-measure, boundary retrieval recall rate, 
%   boundary retrieval precision rate, 
%   median true-to-guess, median guess-to-true
%
% excludelist is a cell array of strings of labels that will be ignore in the
% evaluation (the borders associated to them are ignore in the evals, 
% which may cause some odd behaviour if they occur in the middle of 
% description), can be disabled by providing {''} as the argument

% Jouni Paulus, jouni.paulus@tut.fi, 10.11.2008
% r2 streamlining including hit stats, 14.10.2009, J.P.

if (~exist('deltaT','var') || isempty(deltaT))
  deltaT=3;
end;
if (~exist('excludeList','var'))
  excludeList={'sil','silence','silece','si'};
end;
if (isempty(excludeList))
  excludeList={''};
end;

% get the unique border time stamps for for descriptions
refBorderLocs=bordersFromDesc(refStructure,excludeList);
resBorderLocs=bordersFromDesc(resStructure,excludeList);

refCount=length(refBorderLocs);
resCount=length(resBorderLocs);

% absolute time difference between all border locations
deltaMat=abs(repmat(refBorderLocs(:),1,resCount)-repmat(resBorderLocs(:)',refCount,1));

% minimum distances between true to claimed and vice versa
true2claim=min(deltaMat,[],2);
claim2true=min(deltaMat,[],1);
medianTrue2claim=median(true2claim);
medianClaim2true=median(claim2true);

devCount=length(deltaT);
Fmeasures=zeros(devCount,1);
recRates=zeros(devCount,1);
precRates=zeros(devCount,1);
deltaStore=deltaMat;
for (dIdx=1:devCount)
  deltaMat=deltaStore;
  % assign each border from one set to the closest in other set
  % until either set runs out of candidates
  matchMat=Inf*ones(refCount,resCount);
  didChange=true;
  while (didChange)
    oldMatch=matchMat;
    [minVal,minIdx]=min(deltaMat(:));
    [rowIdx,colIdx]=ind2sub(size(deltaMat),minIdx);
    
    if (isfinite(minVal))
      matchMat(rowIdx,colIdx)=minVal;
      % mark both items as used
      deltaMat(rowIdx,:)=Inf;
      deltaMat(:,colIdx)=Inf;
    end;
  
    didChange=any(any(matchMat~=oldMatch));
  end;
  
  % which of the segment borders are within the allowed delta time
  acceptMatches=matchMat<deltaT(dIdx);
  % number of hits
  totalHits=sum(sum(acceptMatches));
  % and the statistic
  precRate=totalHits/resCount;
  recRate=totalHits/refCount;
  if (precRate+recRate>0)
    Fmeasure=2*precRate*recRate/(precRate+recRate);
  else
    Fmeasure=0;
  end;
  precRates(dIdx)=precRate;
  recRates(dIdx)=recRate;
  Fmeasures(dIdx)=Fmeasure;
end;


function borderTimes=bordersFromDesc(inDesc,excludeList)
%
% borderTimes=bordersFromDesc(inDesc,excludeList)
%
borderTimes=[];
for (partIdx=1:length(inDesc.times))  
  thisTimes=inDesc.times{partIdx};

  if (~ismember(inDesc.labels{partIdx},excludeList))
    borderTimes=[borderTimes; thisTimes(:)];
  end;
end;
borderTimes=unique(borderTimes);

