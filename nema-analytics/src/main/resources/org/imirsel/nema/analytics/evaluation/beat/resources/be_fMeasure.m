function [f,p,r,a] = be_fMeasure(anns,beats,params)

%  function [f,p,r,a] = be_fMeasure(anns,beats,params)
%   
%  Description:
%  Calculate the F-measure as used in (Dixon, 2006) and (Dixon, 2007).
%   
%  Inputs: 
%  anns - sequence of ground truth beat annotations (in seconds)
%  beats - sequence of estimated beat times (in seconds)
%  params - structure of beat evaluation parameters
%   
%  Ouputs: 
%  f - the F-measure
%  p - precision
%  r - recall
%  a - Dixon's related accuracy measure from (Dixon, 2001)
%   
%  References:
%  S. Dixon, "Automatic extraction of tempo and beat from expressive performances," Journal of New Music Research, vol. 30, pp. 39–58, 2001.
%  S. Dixon, "Onset detection revisited," in Proceedings of 9th International Conference on Digital Audio Effects (DAFx), Montreal, Canada, pp. 133–137, 2006.
%  S. Dixon, "Evaluation of audio beat tracking system beatroot," Journal of New Music Research, vol. 36, no. 1, pp. 39–51, 2007.
%   
%  (c) 2009 Matthew Davies


if nargin<3
  params = be_params;
end


% put the beats and annotations in column vectors
anns = anns(:);
beats = beats(:);

% remove beats and annotations that are within the first 5 seconds
anns(anns<params.minBeatTime) = [];
beats(beats<params.minBeatTime) = [];


% Check if there are any beats, if not then exit
if(isempty(beats))
  disp('beat sequence is empty, assigning zero to all outputs [f,p,r,a]');
  f = 0;
  p = 0;
  r = 0;
  a = 0;
  return;
end

% also check that the beat times are in seconds and not any other time units
if( or( (max(beats)>1000) , (max(anns)>1000) ) )
  error('either beats or annotations are not in seconds, please rectify.');
  return;
end
 

% get the threshold parameter for the tolerance window
delta = params.fMeasure.thresh;


% number of false positives
fp = 0;

% number of false negatives
fn = 0;

% number of correct detections
hits= 0;

for i=1:length(anns)
   
  % set up range of tolerance window 
  windowMin = anns(i) - delta;
  windowMax = anns(i) + delta;

  % find those beats which are in the range of the tolerance window
  [a1,a2,a3] = find(and(beats>=windowMin, beats<=windowMax));

  % now remove those beats so they can't be counted again
  beats(a1) = [];      

  if(isempty(a1)), % no beats in window, therefore it's a false negative
      fn = fn +1;
      
  elseif(length(a1)>1)       % false positive case, more than one beat in a tolerance window
      hits = hits+1;
      fp  = fp+ 1;

  else % only one beat in the tolerance window therefore a correct detection
      hits = hits+1;
  end

end


% add any remaining beats to the number of false positives
fp = fp + length(beats);

% calculate precision, p
if(hits+fp)
  p = 100*(hits/(hits+fp));
else
  p = 0;
end

% calculate recall, r
if(hits+fn)
  r = 100*(hits/(hits+fn));
else
  r = 0;
end

% now calculate the f-measure
if(p+r)
  f = 2*p*r/(p+r);
else
  f = 0;
end

% this is Dixon's related accuracy measure from (Dixon, 2001)
if(hits+fp+fn)
  a = 100*hits/(hits+fp+fn);
else
  a = 0;
end

