function params = be_parms;

%  function [params] = be_params
%   
%  Description:
%  Specify parameters for different beat evaluation methods.
%   
%  Inputs: 
%  None
%   
%  Ouputs: 
%  params - a structure of parameters 
%     
%  (c) 2009 Matthew Davies

% ignore all beats up to this point in seconds
params.minBeatTime = 5;
% size of tolerance window for fmeasure
params.fMeasure.thresh = 0.07;
% standard deviation of gaussian error function
params.cemgilAcc.sigma = 0.04;
% range of time-limited cross-correlation for pscore
params.pScore.thresh = 0.2;
% maximum allowed relative beat error
params.gotoAcc.thresh = 0.35;
% maximum allowed mean beat error
params.gotoAcc.mu = 0.2;
% maximum allowed standard deviation of beat error
params.gotoAcc.sigma = 0.2;
% size of tolerance window for beat phase in continuity based evaluation
params.continuityBased.phaseThresh = 0.175;
% size of tolerance window for beat period in continuity based evaluation
params.continuityBased.periodThresh = 0.175;
% number of histogram bins for information gain method
params.informationGain.numBins = 40;
% range of fixed time offsets over which to test beat tracking algorithms  
params.generateResults.offsetRange = 0.01161*[-6:6];
% number of per file evaluation scores to find
params.generateResults.numMethods = 9;
% flag to indicate whether to truncate beats to range of annotations
params.generateResults.truncate = 0;
% flag to indicate whether to show plots or not
params.generateResults.plotting = 1;
% number of bootstrap samples to take
params.confidenceInterval.numSamples = 1000;
% confidence interval
params.confidenceInterval.interval = 0.95;

