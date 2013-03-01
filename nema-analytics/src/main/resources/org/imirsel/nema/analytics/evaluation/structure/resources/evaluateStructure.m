function evaluateStructure(gtFile,algoFile,outFile)

gtRes=readStructureFromLab(gtFile);
algoRes=readStructureFromLab(algoFile);

[overSegScore,underSegScore]=condEntropyEval(gtRes,algoRes);
disp('condEntropyEval DONE');
[pwF,pwPrecision,pwRecall]=pairwiseF(gtRes,algoRes);
disp('pairwiseF DONE')
[R,aVal,bVal,cVal,dVal]=randClusteringIndex(gtRes,algoRes);
disp('randClusteringIndex DONE')
[Fmeasure05,precRate05,recRate05,medianTrue2claim05,medianClaim2true05]=segmentRetrievalEval2(gtRes,algoRes,0.5);
disp('segmentRetrievalEval2-0.5 DONE')
[Fmeasure3,precRate3,recRate3,medianTrue2claim3,medianClaim2true3]=segmentRetrievalEval2(gtRes,algoRes,3);
disp('segmentRetrievalEval2-3 DONE')

evalreport = [overSegScore, underSegScore, pwF, pwPrecision, pwRecall, R, Fmeasure05, precRate05, recRate05, Fmeasure3, precRate3, recRate3, medianTrue2claim3, medianClaim2true3];
dlmwrite(outFile, evalreport);
