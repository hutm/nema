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

