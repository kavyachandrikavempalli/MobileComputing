% Load data from Excel file
User_data = readmatrix('User_profiles.xlsx');

% Extract HR and RR data for LCW and HCW
lcw_hr = User_data(:, [2, 3]);
lcw_rr = User_data(:, [4, 5]);
hcw_hr = User_data(:, [6, 7]);
hcw_rr = User_data(:, [8, 9]);


% Number of users
users_sample = size(User_data, 1);

% Initialize arrays to store random samples
lcw_hr_values = zeros(users_sample, 50);
lcw_rr_values = zeros(users_sample, 50);
hcw_hr_values = zeros(users_sample, 50);
hcw_rr_values = zeros(users_sample, 50);

% Generate random samples for each user
for i = 1:users_sample
    % Generate random samples for LCW HR and RR using Gaussian distribution
    lcw_hr_values(i,:) = normrnd(lcw_hr(i, 1), lcw_hr(i, 2),1,50);
    lcw_rr_values(i,:) = normrnd(lcw_rr(i, 1), lcw_rr(i, 2),1,50);
    
    % Generate random samples for HCW HR and RR using Gaussian distribution
    hcw_hr_values(i,:) = normrnd(hcw_hr(i, 1), hcw_hr(i, 2),1,50);
    hcw_rr_values(i,:) = normrnd(hcw_rr(i, 1), hcw_rr(i, 2),1,50);
    
end

% Calculate the values for Average User
Hr_lcw = mean(mean(lcw_hr_values));
Rr_lcw = mean(mean(lcw_rr_values));
Rq_lcw=Hr_lcw/Rr_lcw;
tr_lcw=0.01*Rq_lcw;

Hr_hcw = mean(mean(hcw_hr_values));
Rr_hcw = mean(mean(hcw_rr_values));
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
time_of_collision=zeros(tune_sample_size(1),1);

Case = randi([1,2]);
for Init_Speed=20:40
    decelLim_lcw = -200;
    decelLim_hcw = -150;
    if Case == 1
        decelLimit = decelLim_lcw;
        tr = tr_lcw;
        Gain=210000;
    else
        decelLimit = decelLim_hcw;
        tr = tr_hcw;
        Gain=210000;
    end
    [A,B,C,D,Kess, Kr, Ke, uD] = designControl(secureRand(),Gain);
    load_system('LaneMaintainSystem.slx')
    
    set_param('LaneMaintainSystem/VehicleKinematics/Saturation','LowerLimit',num2str(decelLimit))
    set_param('LaneMaintainSystem/VehicleKinematics/vx','InitialCondition',num2str(Init_Speed))
    
    simModel1 = sim('LaneMaintainSystem.slx');
    
    if max(simModel1.sx1.data) < 0
        disp("Do Not Switch: No Collision. Gain= "+Gain+" Initial Speed: "+Init_Speed);
        dns=dns+1;
    else
        load_system('VehicleModelOnly.slx')
        set_param('VehicleModelOnly/VehicleKinematics/Saturation','LowerLimit',num2str(decelLimit))
        set_param('VehicleModelOnly/VehicleKinematics/vx','InitialCondition',num2str(Init_Speed))
        set_param('VehicleModelOnly/Step','Time',num2str(tr))
        set_param('VehicleModelOnly/Step','After',num2str(1.1*decelLimit))
        set_param('VehicleModelOnly/VehicleKinematics/Saturation','LowerLimit',num2str(1.1*decelLimit))
        
        simModel2 = sim('VehicleModelOnly.slx');
        if max(simModel2.deceleration.time) < max(simModel1.sx1.Time)
            disp("Switch to Human. Gain= "+Gain+" Initial speed= "+Init_Speed);
        else
            disp("Do Not switch: Collision. Gain= "+Gain+" Initial speed= "+Init_Speed);
        end
    end
end