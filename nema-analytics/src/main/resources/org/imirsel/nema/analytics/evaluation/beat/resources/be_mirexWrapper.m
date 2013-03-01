function [scores,binValues] = be_mirexWrapper(gtFile, algoFile, outfile)

% load parameters
params = be_params;

% read algorithm output
beats = textread(algoFile, '', 'commentstyle', 'shell');

% read possible multi-annotated ground truth, and put each annotation in a
% cell called gt
temp = textread(gtFile, '', 'commentstyle', 'shell');
for i=1:size(temp,2),
    tt = temp(:,i); tt(isnan(tt)) = [];
    gt{i} = tt;
end


numAnnots = length(gt);
numBins = params.informationGain.numBins;
numMethods = params.generateResults.numMethods;
perFileBinVals = zeros(1,numBins);

% loop through each annotation and accumilate results
for annots = 1:numAnnots
   [rawScores{annots}, binVals{annots}] = be_evalWrapper(gt{annots},beats,params,0,0);
   perFileBinVals = perFileBinVals + binVals{annots}; % if we want histograms for each file. 
end

% take the mean of the results across all annotations to generate the
% summary result for this file
perFileScores = zeros(1,length(rawScores{1}));
for annots = 1:numAnnots
    perFileScores = perFileScores + (1/numAnnots) * rawScores{annots};         
end

scores = perFileScores;
binValues = perFileBinVals;

dlmwrite(outfile, [perFileScores, perFileBinVals], 'delimiter', ',');