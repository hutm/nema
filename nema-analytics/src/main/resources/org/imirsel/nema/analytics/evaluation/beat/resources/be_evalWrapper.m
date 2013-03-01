function [scores,binVals] = be_evalWrapper(anns,beats,params,offset,truncate)

%  function [scores,binVals] = be_evalWrapper(anns,beats,params,offset,truncate)
%   
%  Description:
%  Evaluation wrapper function to get scores for all evaluation methods
%   
%  Inputs: 
%  anns - ground truth annotations (in seconds)
%  beats - estimated beat locations (in seconds)
%  offset - a fixed time offset that can be added to each beat
%  truncate - flag to indicate whether to remove all beats outside of the range of annotations
%
%  Ouputs: 
%  scores - beat tracking scores for each evaluation method 
%  binVals - beat error histogram bin values 
%     
%  (c) 2009 Matthew Davies

if nargin<3
  params = be_params;
end

% put the beats and annotations into column vectors
anns = anns(:);
beats = beats(:);

% remove beats and annotations that are within the first 5 seconds
anns(anns<params.minBeatTime) = [];
beats(beats<params.minBeatTime) = [];

numBins = params.informationGain.numBins;


if isempty(beats)
  disp('beat sequence is empty, assigning zero to output values');
  disp('and a uniform beat error histogram');
  scores = zeros(1,9); % there are 9 per-excerpt evaluation scores 
  binVals = zeros(1,numBins) + length(anns)/numBins;
  return;
end

% also check that the beat times are in seconds and not any other time units
if( or( (max(beats)>1000) , (max(anns)>1000) ) )
  error('either beats or annotations are not in seconds, please rectify.');
  return;
end
 

if nargin<4
	offset = 0;
end

if offset
  beats = beats + offset;
end

if nargin<5
  truncate = 0;
end

if truncate
  beats(beats<min(anns)) = [];
  beats(beats>max(anns)) = [];
end


scores = zeros(1,9);

scores(1) = be_fMeasure(anns,beats,params);
scores(2) = be_cemgilAcc(anns,beats,params);
scores(3) = be_gotoAcc(anns,beats,params);
scores(4) = be_pScore(anns,beats,params);
[scores(5),scores(6),scores(7),scores(8)] = be_continuityBased(anns,beats,params);
[scores(9),binVals] = be_informationGain(anns,beats,params);

