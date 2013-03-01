function [lci,uci] = be_confidenceIntervals(scores,params);

%  function [lci,uci] = be_confidenceIntervals(scores,params);
%
%   
%  Description:
%  Work out bootstrapping confidence interval
%   
%  Inputs: 
%  beat tracking scores 
%  params - a structure of parameters 
%   
%  Ouputs: 
%  lci - lower confidence interval
%  uci - upper confidence interval
%     
%  (c) 2009 Matthew Davies


if nargin<2
  params = be_params;
end

% number of bootstrap samples
numSamples = params.confidenceInterval.numSamples;

% confidence interval
interval = params.confidenceInterval.interval;

numScores = length(scores);


for sample = 1:numSamples,

  % get random sampling from scores
  randomSamples = sort(ceil(numScores*rand(1,numScores)));    
  meanVals(sample) = mean(scores(randomSamples));

end

%sort the meanVals

meanVals = sort(meanVals);

%get first and last point of confidence interval

first = ceil(numSamples*(1-interval)/2);
last = ceil(numSamples*interval + numSamples*(1-interval)/2);

lci = meanVals(first);
uci = meanVals(last);
