function [cmlC,cmlT,amlC,amlT] = be_continuityBased(anns,beats,params)

%  function [a] = be_continuityBased(anns,beats,params)
%   
%  Description:
%  Calculate the continuity based accuracy values as used in (Hainsworth, 2004) and (Klapuri et al, 2006)
%   
%  Inputs: 
%  anns - sequence of ground truth beat annotations (in seconds)
%  beats - sequence of estimated beat times (in seconds)
%  params - structure of beat evaluation parameters
%   
%  Ouputs: 
%  cmlC - beat tracking accuracy, continuity required at the correct metrical level 
%  cmlT - beat tracking accuracy, continuity not required at the correct metrical level
%  amlC - beat tracking accuracy, continuity required at allowed metrical levels 
%  amlT - beat tracking accuracy, continuity not required at allowed metrical levels
%   
%  References:
%  S. Hainsworth, "Techniques for the automated analysis of musical audio," Ph.D. dissertation, Department of Engineering, Cambridge University, 2004.
%  A. P. Klapuri, A. Eronen, and J. Astola, "Analysis of the meter of acoustic musical signals," IEEE Transactions on Audio, Speech and Language Processing, vol. 14, no. 1, pp. 342–355, 2006.
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
  disp('beat sequence is empty, assigning zero to all outputs [cmlC,cmlT,amlC,amlT]');
  cmlC = 0;
  cmlT = 0;
  amlC = 0;
  amlT = 0;

  return;
end

% also check that the beat times are in seconds and not any other time units
if( or( (max(beats)>1000) , (max(anns)>1000) ) )
  error('either beats or annotations are not in seconds, please rectify.');
  return;
end
 

p = params.continuityBased.phaseThresh;
t = params.continuityBased.periodThresh;

doubleAnns = interp1([1:length(anns)],anns,[1:0.5:length(anns)]);

% make different variants of annotations
% normal annotations
testAnns{1} = anns;
% off-beats 
testAnns{2} = doubleAnns(2:2:end);
% double tempo
testAnns{3} = doubleAnns;
% half tempo odd-beats (i.e. 1,3,1,3)
testAnns{4} = anns(1:2:end);
% half tempo even-beat (i.e. 2,4,2,4)
testAnns{5} = anns(2:2:end);


% loop analysis over number of variants on annotations
for j=1:size(testAnns,2),

  [totAcc(j),contAcc(j)] = ContinuityEval(testAnns{j},beats,t,p);

end


% assign the accuracy scores
cmlC = contAcc(1);
cmlT = totAcc(1);
amlC = max(contAcc);
amlT = max(totAcc);

function [totAcc,contAcc] = ContinuityEval(anns,beats,t,p)
% sub-function for calculating continuity-based accuracy


checkanns = zeros(1,max(length(anns),length(beats)));
checkbeats = checkanns;

cond = 0;



for i=1:length(beats),

    cond = 0;
    [a,b] = min(abs(beats(i)-anns)); % look for nearest annotation to current beat

    if(checkanns(b)==1) % if we've already used this annotation..
        cond = 0;
    else
        
        if or(i==1,b==1), % either first beat or first annotation, look forward on both
            if ( ( (b+1) <= length(anns) ) && ( (i+1) <= length(beats) ) )
                if (and((abs(a/(anns(b+1)-anns(b)))<p), ...
                        (abs(1-((beats(i+1)-beats(i))/(anns(b+1)-anns(b))))<t)));
                    checkanns(b) = 1;
                    cond = 1;
                end
            end

        else % not first beat or first annotation, so can look backwards
            
            if ( ( (b-1) >= 1 ) && ( (i-1) >= 1 ) )
                if (and((abs(a/(anns(b)-anns(b-1)))<p), ...
                        (abs(1-((beats(i)-beats(i-1))/(anns(b)-anns(b-1))))<t)));
                    checkanns(b) = 1;
                    cond = 1;
                end
            end

        end

    end

    % for i^th beat what is the condition
    checkbeats(i) = double(cond);

end

checkbeats = [0 checkbeats(:)' 0];

[d1,d2,d3] = find(checkbeats==0);
checkbeats = checkbeats(2:end-1);

% in best case, d2 = 1 & length(checkbeats)
contAcc = 100*(max(diff(d2))-1)/length(checkbeats);
totAcc = 100*sum(checkbeats)/length(checkbeats);
