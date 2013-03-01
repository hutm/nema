function [p] = be_pScore(anns,beats,params)

%  function [p] = be_pScore(anns,beats,params)
%   
%  Description:
%  Calculate the McKinney et al's PScore accuracy value as used in (McKinney et al, 2007).
%   
%  Inputs: 
%  anns - sequence of ground truth beat annotations (in seconds)
%  beats - sequence of estimated beat times (in seconds)
%  params - structure of beat evaluation parameters
%   
%  Ouputs: 
%  p - beat tracking accuracy 
%   
%  References:
%  
%  M. F. McKinney, D. Moelants, M. E. P. Davies, and A. Klapuri, "Evaluation of audio beat tracking and music tempo extraction algorithms," Journal of New Music Research, vol. 36, no. 1, pp. 1–16, 2007.
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


% Check if there are any beats, if not then exit
if(isempty(beats))
  disp('beat sequence is empty, assigning zero to all outputs [p]');
  p = 0;
  return;
end

% also check that the beat times are in seconds and not any other time units
if( or( (max(beats)>1000) , (max(anns)>1000) ) )
  error('either beats or annotations are not in seconds, please rectify.');
  return;
end
 

thresh = params.pScore.thresh;

% quantize beats to 10ms resolution
fs = 100;

endPoint = ceil(max([anns' beats']));

% make impulse trains from beat times, take those beats only greater than 5 seconds
annsTrain = zeros(endPoint*fs,1);
annsTrain(ceil(anns*fs)) = 1;
beatsTrain = zeros(endPoint*fs,1);
beatsTrain(ceil(beats*fs)) = 1;

W = round(thresh*median(diff(find(annsTrain))));
p = sum(xcorr(beatsTrain,annsTrain,W))/max(length(find(beatsTrain)),length(find(annsTrain)));

p = 100*p;

