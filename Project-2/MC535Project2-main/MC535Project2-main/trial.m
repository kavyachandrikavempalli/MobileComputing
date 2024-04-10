User_data = readmatrix('User_profiles.xlsx');

% Extract HR and RR data for LCW and HCW
lcw_hr = User_data(:, [2, 4]);
lcw_rr = User_data(:, [3, 5]);
hcw_hr = User_data(:, [6, 8]);
hcw_rr = User_data(:, [7, 9]);


% Number of users
users_sample = size(User_data, 1);

% Initialize arrays to store random samples
lcw_hr_values = zeros(users_sample, 1);
lcw_rr_values = zeros(users_sample, 1);
hcw_hr_values = zeros(users_sample, 1);
hcw_rr_values = zeros(users_sample, 1);

% Generate random samples for each user
for i = 1:users_sample
    % Generate random samples for LCW HR and RR using Gaussian distribution
    lcw_hr_values(i) = normrnd(lcw_hr(i, 1), lcw_hr(i, 2));
    lcw_rr_values(i) = normrnd(lcw_rr(i, 1), lcw_rr(i, 2));
    
    % Generate random samples for HCW HR and RR using Gaussian distribution
    hcw_hr_values(i) = normrnd(hcw_hr(i, 1), hcw_hr(i, 2));
    hcw_rr_values(i) = normrnd(hcw_rr(i, 1), hcw_rr(i, 2));
end

% Calculate the values for Average User
Hr_lcw = mean(lcw_hr_values);
Rr_lcw = mean(lcw_rr_values);
Rq_lcw=Hr_lcw/Rr_lcw;
tr_lcw=0.01*Rq_lcw;

Hr_hcw = mean(hcw_hr_values);
Rr_hcw = mean(hcw_rr_values);
Rq_hcw=Hr_hcw/Rr_hcw;
tr_hcw=0.01*Rq_hcw;

Switch = 0;
dns=0;
dns1=0;
timecount=0;
Collision=0;
p=1;
Excel_data=readmatrix('p2.xlsx');
tune_sample_size=size(Excel_data);
time_of_collision=zeros(21,1);

for i=1:tune_sample_size(1)
    Update=0;
    for j=1:tune_sample_size(1)
     Gain(j,1)=Excel_data(j,1);
     Init_Speed(i,1)=Excel_data(i,2); 
    
    %Gain = 10000;
    %InitSpeed = 100;

    decelLim_lcw = -200;
    decelLim_hcw = -150;
    Case = randi([1, 2]);
    %disp(Case);
    
    if Case == 1
        decelLimit = decelLim_lcw;
        tr = tr_lcw;
        disp('lcw');
    else
        decelLimit = decelLim_hcw;
        tr = tr_hcw;
        disp('hcw')
    end
    end
end