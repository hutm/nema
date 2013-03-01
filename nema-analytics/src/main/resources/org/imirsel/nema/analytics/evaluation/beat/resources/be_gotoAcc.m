function [a] = be_gotoAcc(anns,beats,params)

%  function [a] = be_gotoAcc(anns,beats,params)
%   
%  Description:
%  Calculate the Goto and Muraoka's accuracy value as used in (Goto and Muraoka, 1997).
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
%  M. Goto and Y. Muraoka, "Issues in evaluating beat tracking systems," in Working Notes of the IJCAI-97 Workshop on Issues in AI and Music - Evaluation and Assessment, 1997, pp. 9–16.
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
 

thresh = params.gotoAcc.thresh;
mu = params.gotoAcc.mu;
sigma = params.gotoAcc.sigma;


beatError = ones(length(anns),1);

paired = 0*anns;

hit = 0;


for k=2:length(anns)-1,

  % previous inter annotation interval
  preInterval = 0.5*(anns(k)-anns(k-1));
  % beginning of half i-a-i
  windowMin = anns(k) - preInterval;

  % next inter annotation interval
  postInterval = 0.5*(anns(k+1)-anns(k));
  % end of half i-a-i
  windowMax = anns(k) + postInterval;

  % find those beats which are in the range of windowMin to windoMax
  [a1,a2,a3] = find(and(beats>=windowMin, beats<windowMax));



  if(isempty(a1)),
    % false negative case 
    paired(k) = 0;
    beatError(k) = 1;

  elseif(length(a1)>1)
    % false positive case.
    paired(k) = 0;
    beatError(k) = 1;

  else
    % it's paired beat, so measure beat error
    paired(k) = 1;

    newError = beats(a1) - anns(k);

    if (newError<0) % if negative use pre_interval
        beatError(k) = newError/preInterval;
    else % else positive so use subsequent inter annotation interval
        beatError(k) = newError/postInterval;
    end

  end        
    
end


[b1,b2,b3] = find(abs(beatError)>thresh);

if length(b1)<3 % i.e. if only first and last

  % extract track
  track = beatError(b1(1)+1:b1(end)-1);
  hit = hit+1; % have shown good until the end
    
else
    
  [trackLen,ind] = max(diff(b1));
  
  if (trackLen-1) > 0.25*(length(anns)-2),
    % i.e. we have more than 25% correct tracking.. somewhere
    hit = hit+1;
    track = beatError(b1(ind):b1(ind+1));
  end

    
end

    % if have found a hit so far
if hit,
    % now test mean and standard deviation
    
  if (and( (mean(abs(track))<mu), (std(track)<sigma) ))
    hit = hit+2;
  end
    
end % hit stays the same
    
   
if (hit==3)
  a = 100;
else
  a = 0;
end
    
