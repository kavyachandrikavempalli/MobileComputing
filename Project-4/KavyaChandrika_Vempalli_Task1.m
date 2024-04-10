%% Load data from Excel file
User_data=[1,80,14,16,6,95,26,21,14;2,65,15,13,4,71,21,14,5;3,61,14,17,8,92,23,26,16];

%% Extract HR and RR data for LCW and HCW
lcw_hr = User_data(:, [2, 3]);
lcw_rr = User_data(:, [4, 5]);
hcw_hr = User_data(:, [6, 7]);
hcw_rr = User_data(:, [8, 9]);

%% Number of users
users_sample = size(User_data, 1);

%% Initialize arrays to store random samples
lcw_hr_values = zeros(users_sample, 50);
lcw_rr_values = zeros(users_sample, 50);
hcw_hr_values = zeros(users_sample, 50);
hcw_rr_values = zeros(users_sample, 50);

%% Generate random samples for each user
for i = 1:users_sample
    lcw_hr_values(i,:) = normrnd(lcw_hr(i, 1), lcw_hr(i, 2),1,50);
    lcw_rr_values(i,:) = normrnd(lcw_rr(i, 1), lcw_rr(i, 2),1,50);
    hcw_hr_values(i,:) = normrnd(hcw_hr(i, 1), hcw_hr(i, 2),1,50);
    hcw_rr_values(i,:) = normrnd(hcw_rr(i, 1), hcw_rr(i, 2),1,50);
    
end

%% Calculate the values for Average User
Hr_lcw = mean(mean(lcw_hr_values));
Rr_lcw = mean(mean(lcw_rr_values));
Rq_lcw=Hr_lcw/Rr_lcw;
tr_lcw=0.01*Rq_lcw;

Hr_hcw = mean(mean(hcw_hr_values));
Rr_hcw = mean(mean(hcw_rr_values));
Rq_hcw=Hr_hcw/Rr_hcw;
tr_hcw=0.01*Rq_hcw;

%% Initializing loop variables
Switch = zeros(2,1);
dns=0;
dns1=0;
timecount=0;
Collision=0;
p=1;
time_of_collision=zeros(21,1);

%% For loop
for Case=1:2
for Init_Speed=20:40
    Update=0;
    Gain_lcw=85000;
    Gain_hcw=100000;
    decelLim_lcw = -200;
    decelLim_hcw = -150;
    if Case == 1
        decelLimit = decelLim_lcw;
        tr = tr_lcw;
        Gain=Gain_lcw;
        disp('lcw');
    else
        decelLimit = decelLim_hcw;
        tr = tr_hcw;
        Gain=Gain_hcw;
        disp('hcw')
    end
    [A,B,C,D,Kess, Kr, Ke, uD] = designControl(secureRand(),Gain);
    load_system('LaneMaintainSystem.slx')
    
    set_param('LaneMaintainSystem/VehicleKinematics/Saturation','LowerLimit',num2str(decelLimit))
    set_param('LaneMaintainSystem/VehicleKinematics/vx','InitialCondition',num2str(Init_Speed))
    
    simModel1 = sim('LaneMaintainSystem.slx');
    distance=simModel1.sx1.data;
    time=simModel1.sx1.Time;
    for z=1:size(distance,1)
         if distance(z,1)>0 && Update==0
              Update=1;
              time_of_collision(p,1)=time(z-1,1);
              tc=time_of_collision(p,1);
              p=p+1;
              disp("Time of collision: "+tc);
         end
    end
    if max(simModel1.sx1.data) < 0
        disp("Do Not Switch: No Collision. Gain= "+Gain+" Initial Speed: "+Init_Speed);
        dns=dns+1;
    else
        load_system('HumanActionModel.slx')
        %set_param('HumanActionModel/VehicleKinematics/Saturation','LowerLimit',num2str(decelLimit))
        set_param('HumanActionModel/VehicleKinematics/vx','InitialCondition',num2str(Init_Speed))
        set_param('HumanActionModel/Step','Time',num2str(tr))
        set_param('HumanActionModel/Step','After',num2str(1.1*decelLimit))
        set_param('HumanActionModel/VehicleKinematics/Saturation','LowerLimit',num2str(1.1*decelLimit))
        
        simModel2 = sim('HumanActionModel.slx');
        if max(simModel2.deceleration.time) < tc %hstop<tstop
            disp("Switch to Human. Gain= "+Gain+" Initial speed= "+Init_Speed);
            Switch(Case,1) = Switch(Case,1) + 1;
        else
            disp("Do Not switch: Collision. Gain= "+Gain+" Initial speed= "+Init_Speed);
            Collision=Collision+1;
            dns1=dns1+1;
        end
    end
end
disp("Gain: "+i+" is done")
disp("Switches: "+Switch(Case,1))
disp("Collisions:"+Collision)
disp("DNS: "+dns+" DNS1: "+dns1)

end

%% Output to excel
% 1.1 User profile: lcw_hr, lcw_rr, hcw_hr, hcw_rr
% 1.2 Reaction time model: lcw_rq, hcw_rq
% 1.3 Gain: lcw_gain, hcw_gain
% 1.4 No of switches

Result= string;
Result(1,1)='Average User Profile';
Result(1,2)='Task-1.1';
Result(2,1)='Low Cognitive load HR';
Result(2,2)=Hr_lcw;
Result(3,1)='Low Cognitive load RR';
Result(3,2)=Rr_lcw; 
Result(4,1)='High Cognitive load HR';
Result(4,2)=Hr_hcw;
Result(5,1)='High Cognitive load RR';
Result(5,2)=Rr_hcw;
Result(6,1)='';
Result(6,2)='';
Result(7,1)='Reaction time';
Result(7,2)='Task-1.2';
Result(8,1)='Low Cognitive load TR';
Result(8,2)=tr_lcw;
Result(9,1)='High Cognitive load TR';
Result(9,2)=tr_hcw;
Result(10,1)='';
Result(10,2)='';
Result(11,1)='Gain';
Result(11,2)='Task-1.3';
Result(12,1)='Low Cognitive Gain';
Result(12,2)= Gain_lcw;
Result(13,1)='High Cognitive Gain';
Result(13,2)= Gain_hcw;
Result(14,1)='';
Result(14,2)='';
Result(15,1)='Number of Switches';
Result(15,2)='Task-1.4';
Result(16,1)='Low Cognitive load';
Result(16,2)=Switch(1,1);
Result(17,1)='High Cognitive load';
Result(17,2)=Switch(2,1);
writematrix(Result,'KavyaChandrika_Vempalli.xls','Sheet',1);
