function [D,binVals,fwdEntropy,bwdEntropy,fwdBinVals,bwdBinVals,histBins,fwdError,bwdError] = be_informationGain(anns,beats,params)

%  function [D,binVals] = be_informationGain(anns,beats,params)
%   
%  Description:
%  Calculate the information gain as used in (Davies et al, 2009).
%   
%  Inputs: 
%  anns - sequence of ground truth beat annotations (in seconds)
%  beats - sequence of estimated beat times (in seconds)
%  params - structure of beat evaluation parameters
%   
%  Ouputs: 
%  D - beat tracking information gain 
%  binVals - beat error histogram bin values 
% 
%  References:
%  
%  M. E. P. Davies, N. Degara and M. D. Plumbley, "Evaluation methods for musical audio beat tracking algorithms," submitted to IEEE TASLP, 2009.
%   
%  (c) 2009 Matthew Davies


if nargin<3
  params = be_params;
end


% put the beats and annotations into column vectors
anns = sort(anns(:));
beats = sort(beats(:));

% remove beats and annotations that are within the first 5 seconds
anns(anns<params.minBeatTime) = [];
beats(beats<params.minBeatTime) = [];


numBins = params.informationGain.numBins;


% Check if there are any beats, if not then exit
if(isempty(beats))
  disp('beat sequence is empty, assigning zero to information gain [D]');
  disp('and a uniform beat error histogram');
  D = 0;
  % slight stretch here, to get totally uniform, use non-integer bin heights
  binVals = zeros(1,numBins) + length(anns)/numBins;
  return;
end

% also check that the beat times are in seconds and not any other time units
if( or( (max(beats)>1000) , (max(anns)>1000) ) )
  error('either beats or annotations are not in seconds, please rectify.');
  return;
end
 


histBins = [-.5, (-0.5+0.5*(1/(numBins-1))):1/(numBins-1):(.5-0.5*(1/(numBins-1))),.5];
% actually gives numbins+1 bins..

% beats compared to annotations
fwdError = FindBeatError_nor(anns,beats);
[fwdEntropy,fwdBinVals] = FindEntropy(fwdError,histBins);

% annotations compared to beats
bwdError = FindBeatError_nor(beats,anns);
[bwdEntropy,bwdBinVals] = FindEntropy(bwdError,histBins);


% find higher entropy value (i.e. which is worst)
if fwdEntropy > bwdEntropy
  maxEntropy = fwdEntropy;
  binVals = fwdBinVals;
else
  maxEntropy = bwdEntropy;
  binVals = bwdBinVals;
end

% % Matthew change 6/11/09
% maxEntropy = bwdEntropy;
% binVals = bwdBinVals;

% CHANGE: the center of the histogram bins is histBins = histBins(1:end-1);
% output bins
% % % histBins = [-.5:1/(numBins-1):0.5]; NO!!!
histBins = histBins(1:end-1);
  
D = log2(numBins) - maxEntropy;



function beatError = FindBeatError_nor(anns,beats)

beatError = zeros(size(beats));
anns = sort(anns);
beats = sort(beats);
% Calculate relative error for each beat sample
for i = 1:length(beats)
    [tmp,i_e] = min(abs(beats(i)-anns)); % find closest annotation to beats(i)
    e_absolute = beats(i)-anns(i_e);
    % test on i_e now..
    if (i_e == 1) % if first annotation is nearest
      if (e_absolute >= 0)
          interval = 0.5*(anns(i_e+1) - anns(i_e));
          e_relative = 0.5* e_absolute / interval;
      else
          % Do not calculate the error if the beat is before the
          % annotation, it might cause error problems
          continue
      end
    elseif (i_e == length(anns)) % if last annotation is nearest
      if (e_absolute <= 0)
          interval = 0.5*(anns(i_e) - anns(i_e-1));
          e_relative = 0.5* e_absolute / interval;
      else
          continue
      end
  %    which = [which 2];
    else % normal case - need to test whether e_absolute is positive or negative.. and chose interval accordingly
      if (e_absolute >= 0) % nearest annotation is BEFORE the current beat - so look at the previous interval
        interval = 0.5*(anns(i_e+1) - anns(i_e));
        e_relative = 0.5* e_absolute / interval;
   %   which = [which 3];
      else  % nearest annotation is AFTER the current beat - so look at the next interval
        interval = 0.5*(anns(i_e) - anns(i_e-1));
        e_relative = 0.5* e_absolute / interval;
    %  which = [which 4];
      end
    end
    beatError(i) = e_relative;
end

% will this hack work???
beatError = round(10000*beatError)/10000;

%'here'
%keyboard; 

function beatError = FindBeatError_nor2(anns,beats)


beatError = zeros(size(beats));
 
anns = sort(anns);
beats = sort(beats);
 
%which = [];
 
% Calculate relative error for each beat sample
for i = 1:length(beats)

    [tmp,i_e] = min(abs(beats(i)-anns)); % find closest annotation to beats(i)
    e_absolute = beats(i)-anns(i_e);

    % test on i_e now..
    if (i_e == 1) % if first annotation is nearest

      interval = 0.5*(anns(i_e+1) - anns(i_e));
      e_relative = 0.5* e_absolute / interval;  
 %     which = [which 1];

    elseif (i_e == length(anns)) % if last annotation is nearest

      interval = 0.5*(anns(i_e) - anns(i_e-1));
      e_relative = 0.5* e_absolute / interval;  
  %    which = [which 2];

    else % normal case - need to test whether e_absolute is positive or negative.. and chose interval accordingly

      if (e_absolute > 0) % nearest annotation is BEFORE the current beat - so look at the previous interval

        interval = 0.5*(anns(i_e) - anns(i_e-1));
        e_relative = 0.5* e_absolute / interval;  
   %   which = [which 3];
      
      else  % nearest annotation is AFTER the current beat - so look at the next interval

        interval = 0.5*(anns(i_e+1) - anns(i_e));
        e_relative = 0.5* e_absolute / interval;  
    %  which = [which 4];

      end

    end

    beatError(i) = e_relative;

end

% will this hack work???
beatError = round(10000*beatError)/10000;

%'here'
%keyboard; 

%numEarly=length(find(beatError<-0.50001)); % was having some problems with rounding errors... 
%numExtra=length(find(beatError>0.50001));

%if (sum(numEarly + numExtra) > 3)
%  [numEarly numExtra]

%end


function [entropy,rawBinVals] = FindEntropy(beatError,histBins)

% fix any overflow values that might have occurred. % no don't want to do this now..
% any beatError values that have overflowed are additional beats... we want these
% to add uniformity to the histogram..

len = length(beatError);
% find the number of beats that are less than -0.5 and greater than 0.5
numEarly=length(find(beatError<-0.50001));
numExtra=length(find(beatError>0.50001));


oldbeatError = beatError;

beatError(beatError>max(histBins)) = [];
beatError(beatError<min(histBins)) = [];

% get bin heights
rawBinVals = hist(beatError,histBins);
% want to add the last bin height to the first bin.
rawBinVals(1) = rawBinVals(1)+rawBinVals(end);
rawBinVals(end) = [];

% now add uniformity in line with extra bins to rawBinVals;
rawBinVals = rawBinVals + (numEarly+numExtra)/length(rawBinVals);

% make sure the bins heights sum to unity
binVals = (rawBinVals+eps);
binVals = binVals/sum(binVals);



% set zero valued binvals to 1, but don't want to output these...
binValsTmp = binVals;
% this makes the entropy calculation well-behaved.
binValsTmp(binValsTmp==0) = 1;


% now calculate the entropy
entropy = -sum(binValsTmp.*log2(binValsTmp));


function phase=princarg(phasein)
%phase=princarg(phasein) maps phasein into the [-pi:pi] range
phase=mod(phasein+pi,-2*pi)+pi;


