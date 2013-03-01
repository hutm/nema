function [a] = be_cemgilAcc(anns,beats,params)

%  function [a] = be_cemgilAcc(anns,beats,params)
%   
%  Description:
%  Calculate the Cemgil et al's accuracy value as used in (Cemgil et al, 2001).
%   
%  Inputs: 
%  anns - sequence of ground truth beat annotations (in seconds)
%  beats - sequence of estimated beat times (in seconds)
%  params - structure of beat evaluation parameters
%   
%  Ouputs: 
%  a - beat tracking accuracy 
%   
%  References:
%  A. T. Cemgil, B. Kappen, P. Desain, and H. Honing, "On tempo tracking: Tempogram representation and Kalman filtering," Journal Of New Music Research, vol. 28, no. 4, pp. 259–273, 2001
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
  disp('beat sequence is empty, assigning zero to all outputs [a]');
  a = 0;
  return;
end

% also check that the beat times are in seconds and not any other time units
if( or( (max(beats)>1000) , (max(anns)>1000) ) )
  error('either beats or annotations are not in seconds, please rectify.');
  return;
end
 

% get standard deviation for Gaussian error function
sigma = params.cemgilAcc.sigma;



% beat accuracy initially zero
a = 0;


for i=1:length(anns)

  % find the closest beat to each annotation
  beatDiff = min(abs(anns(i) - beats));

  % work out the value on the gaussian error function and add to the cumulative sum
  a = a + exp(-(beatDiff.^2)/(2*(sigma^2)));

end

% normalise by the mean of the number of annotations and beats
a = a/(0.5*(length(beats)+length(anns)));

% put into the range 0 to 100%
a = a*100;


