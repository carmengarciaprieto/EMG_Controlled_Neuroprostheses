% ---------------------- CONFIG ----------------------
emg_path = '/Users/carmengarciaprieto/Downloads/EMG_Controlled_Neuroprostheses/EMG_signals/monica200_2025-06-23_13-10-21.txt';
csv_path = '/Users/carmengarciaprieto/Downloads/EMG_Controlled_Neuroprostheses/FES_recordings/monica200_2025-06-23_13-10-21.csv';
fs = 1000;  
manual_offset_ms = 4500; 

% ---------------------- LOAD EMG ----------------------
emg = load(emg_path);
N = length(emg);
t = (0:N-1) / fs;  % time in seconds
time_vector_ms = (0:N-1) * 1000 / fs;  % time in miliseconds

% ---------------------- LOAD FES EVENTS ----------------------
opts = detectImportOptions(csv_path, 'Delimiter', ';');
opts = setvartype(opts, {'emg_timestamp_ms','fes_timestamp_ms','estado'}, {'double','double','string'});
data = readtable(csv_path, opts);

% ---------------------- BUILD FES SIGNAL ----------------------
start_time_ms = data.fes_timestamp_ms(1);   %this gives a value as 17345678987 since starts counting from january 1 1970
timestamps_rel = data.fes_timestamp_ms +  manual_offset_ms - start_time_ms; %it is necessary to adjust each value of the timestampsto a relative time.  

fes_activation = zeros(N,1); %vector full of 0 of the same length as emg indicating fes is initially off
current_state = 0;
last_index = 1;

for i = 1:height(data)
    ts = timestamps_rel(i);
    idx = find(time_vector_ms >= ts, 1, 'first'); 
    if isempty(idx)
        continue;
    end
    fes_activation(last_index:idx) = current_state; %fills from last_index to idx with current_state
    current_state = strcmp(data.estado(i), 'ON'); % if it is ON current_state is 1
    last_index = idx + 1;
end

if last_index <= N
    fes_activation(last_index:end) = current_state;
end

% ---------------------- PLOT ----------------------

fes_offset = 0.01;
fes_scale  = 0.06;
fes_plot = fes_offset + fes_scale * fes_activation;

figure;
plot(t, emg, 'b');
hold on;
plot(t, fes_plot, 'r-', 'LineWidth', 1.5);


yline(0.0265, '--g', 'Threshold ON (0.0265)', 'LabelHorizontalAlignment', 'left');
yline(0.0235, '--m', 'Threshold OFF (0.0235)', 'LabelHorizontalAlignment', 'left');

ylabel('EMG (mV)');
xlabel('Time (s)');
ylim([0, max(emg)*1.2]);

title('EMG Signal with FES Activation Overlay');
legend('EMG', 'FES Activation');
grid on;