function [bestMap,bestCost]=findUniqueMapping(confMat)
%
% [bestMap,bestCost]=findUniqueMapping(confMat)
%
% Find the best 1-to-1 mapping between two label sets, given the confusion 
% matrix.

% Jouni Paulus, jouni.paulus@tut.fi, 3.9.2009

% remove unique mappings that don't need to be considered in the future
binMat=confMat>0;
horSum=sum(binMat,2);
verSum=sum(binMat,1);

maskMat=horSum*verSum;
costMat=binMat.*maskMat;
[rowIndices,colIndices]=find(costMat>2);

bestMap=confMat>0;
bestCost=0;
for (rIdx=1:length(rowIndices))
  tmpConf=confMat;
  % remove one of the collisions, and make a recursive call
  tmpConf(rowIndices(rIdx),colIndices(rIdx))=0;
  [tmpMap,tmpCost]=findUniqueMapping(tmpConf);
  if (tmpCost>bestCost)
    bestCost=tmpCost;
    bestMap=tmpMap;
  end;
end;

% the rest can be decided with max()
tmpCostMat=confMat.*bestMap;
if (size(confMat,1)>size(confMat,2))
  for (cIdx=1:size(confMat,2))
    [costVal,maxIdx]=max(tmpCostMat(:,cIdx));
    if (costVal>0)
      bestMap(:,cIdx)=0;
      bestMap(maxIdx,cIdx)=1;
    end;
  end;
else
  for (rIdx=1:size(confMat,1))
    [costVal,maxIdx]=max(tmpCostMat(rIdx,:));
    if (costVal>0)
      bestMap(rIdx,:)=0;
      bestMap(rIdx,maxIdx)=1;
    end;
  end;
end;

bestCost=sum(sum(confMat.*bestMap));



