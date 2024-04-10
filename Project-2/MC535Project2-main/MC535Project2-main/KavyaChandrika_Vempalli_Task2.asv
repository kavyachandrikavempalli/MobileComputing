%% Load data from Excel file
User_data=[1,80,14,16,6,95,26,21,14;2,65,15,13,4,71,21,14,5;3,61,14,17,8,92,23,26,16];
User_3=User_data(3,:);

%% Extract HR and RR data for LCW and HCW
lcw_hr = User_data(3, [2, 3]);
lcw_rr = User_data(3, [4, 5]);
hcw_hr = User_data(3, [6, 7]);
hcw_rr = User_data(3, [8, 9]);

%% Generate random samples for user 3
lcw_hr_val = normrnd(lcw_hr(1, 1), lcw_hr(1, 2),50,1);
lcw_rr_val = normrnd(lcw_rr(1, 1), lcw_rr(1, 2),50,1);
hcw_hr_val = normrnd(hcw_hr(1, 1), hcw_hr(1, 2),50,1);
hcw_rr_val = normrnd(hcw_rr(1, 1), hcw_rr(1, 2),50,1);

%% Calculate the mean value
Hr_lcw = mean(lcw_hr_val);
Rr_lcw = mean(lcw_rr_val);

Hr_hcw = mean(hcw_hr_val);
Rr_hcw = mean(hcw_rr_val);

%% Initializations
Gain_lcw=0;
Gain_hcw=0;

dns=0;
dns1=0;
timecount=0;
Collision_count=zeros(2,1);
p = 1;
Update=0;

time_of_collision=zeros(100,1);
low_prob=0;
high_prob=0;
i=1;
lcw_count=0;
hcw_count=0;
Speed_Sample_count=0;
Speed_Range=zeros(100,1);
Switch= string;
switch_count=zeros(2,1);
Collision= string;
j=1;

%% mcmc model to generate scenarios
numSteps = 100;
P = [0.6 0.4; 0.85 0.15];
mc = dtmc(P);
scenario = simulate(mc,numSteps);% 2 is HCW, 1 is LCW
scenario1=string(scenario);
scenout=strjoin(strcat(scenario1).','  ');

scenario_size=size(scenario);
%% Calculating probability of each road condition and calculating reaction time setting
for j=1:scenario_size(1)
    if scenario(j)==1
        low_prob=low_prob+1;
    else
        high_prob=high_prob+1;
    end
end
reaction_time_setting=11.5;

%% HR, RR, RQ and TR calculations
HR=((low_prob*Hr_lcw)+(high_prob*Hr_hcw))/100;
RR=((low_prob*Rr_lcw)+(high_prob*Rr_hcw))/100;
RQ=HR/RR;
TR=RQ*0.01*reaction_time_setting;
speed=20:0.404:60;
speedstring=string(speed)';
speedout=strjoin(strcat(speedstring).','  ');

%% For loop
for InitSpeed=20:0.404:60
    Gain_lcw = 85000;
    Gain_hcw= 100000;
    decelLim_lcw = -200;
    decelLim_hcw = -150;
    Case = scenario(i);
    
    if Case == 1
        decelLim = decelLim_lcw;
        Gain=Gain_lcw;
        lcw_count=lcw_count+1;
        disp("Case: LCW");
    else
        decelLim = decelLim_hcw;
        Gain=Gain_hcw;
        hcw_count=hcw_count+1;
        disp("Case: HCW");
    end
    [A,B,C,D,Kess, Kr, Ke, uD] = designControl(secureRand(),Gain);
    load_system("LaneMaintainSystem.slx");
    
    set_param('LaneMaintainSystem/VehicleKinematics/Saturation','LowerLimit',num2str(decelLim))
    set_param('LaneMaintainSystem/VehicleKinematics/vx','InitialCondition',num2str(InitSpeed))
    
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
        disp("Do Not Switch: No Collision. Gain= "+Gain+" Initial Speed: "+InitSpeed);
        dns=dns+1;
    else
        load_system("HumanActionModel.slx");
        set_param('HumanActionModel/VehicleKinematics/Saturation','LowerLimit',num2str(1.1*decelLim))
        set_param('HumanActionModel/VehicleKinematics/vx','InitialCondition',num2str(InitSpeed))
        set_param('HumanActionModel/Step','Time',num2str(TR))
        set_param('HumanActionModel/Step','After',num2str(1.1*decelLim))
        simModel2 = sim('HumanActionModel.slx');
    
        if max(simModel2.deceleration.time) < tc %hstop<tstop
            disp("Switch to Human. Gain= "+Gain+" Initial speed= "+InitSpeed);
            switch_count(Case,1) = switch_count(Case,1) + 1;
        else
            disp("Do Not switch: Collision. Gain= "+Gain+" Initial speed= "+InitSpeed);
            Collision_count(Case,1)=Collision_count(Case,1)+1;
            dns1=dns1+1;
        end
    end
    Speed_Sample_count=Speed_Sample_count+1;
    Speed_Range(i)=InitSpeed;
    i=i+1;
end

%% Output to excel
% 2.1 Scenarios
% 2.2 Speed Sampling
% 2.3 Fix HR, RR, RQ and TR
% 2.4 Collision status
% 2.5 Number of switches
% 2.6 Reaction Time setting
Scenario_output=string;
Scenario_output(1,1)='Scenarios';
Scenario_output(1,2)='Task-2.1';
Scenario_output(2,1)='Low Cognitive Workload Scenarios';
Scenario_output(2,2)=lcw_count;
%Scenario_output(2,2)=strcat('['+scenario+']');
Scenario_output(3,1)='High Cognitive Workload Scenarios';
Scenario_output(3,2)=hcw_count;
Scenario_output(4,1)='Scenarios';
Scenario_output(4,2)=scenout;
Speed_Sampling=string;
Speed_Sampling(1,1)='Speed Samples';
Speed_Sampling(1,2)='Task-2.2';
Speed_Sampling(2,1)='Speed Values';
Speed_Sampling(2,2)=speedout;
Speed_Sampling(3,1)='Step Size';
Speed_Sampling(3,2)='0.404';
Tuned_values=string;
Tuned_values(1,1)='Tuned Parameters';
Tuned_values(1,2)='Task-2.3';
Tuned_values(2,1)='Heart Rate';
Tuned_values(2,2)=HR;
Tuned_values(3,1)='Respiratory Rate';
Tuned_values(3,2)=RR;
Tuned_values(4,1)='Resiratory Quotient';
Tuned_values(4,2)=RQ;
Tuned_values(5,1)='Reaction Time';
Tuned_values(5,2)=TR;
Collision(1,1)='Collision Count';
Collision(1,2)='Task2.4';
Collision(2,1)='No of Collisions in low cognitive work load';
Collision(3,1)='No of Collisions in high cognitive work load';
Collision(2,2)=Collision_count(1,1);
Collision(3,2)=Collision_count(2,1);
Collision(4,1)='Low Cognitive load collision status';
Collision(5,1)='High Cognitive load collision status';
if Collision_count(1,1)>0
    Collision(4,2)='yes';
else
    Collision(4,2)='no';
end
if Collision_count(2,1)>0
    Collision(5,2)='yes';
else
    Collision(5,2)='no';
end
Switch(1,1)='Switches Count';
Switch(1,2)='Task2.5';
Switch(2,1)='No of Switches in low cognitive work load';
Switch(3,1)='No of Switches in high cognitive work load';
Switch(2,2)=switch_count(1,1);
Switch(3,2)=switch_count(2,1);
rt=string;
rt(1,1)='Reaction Time Setting';
rt(1,2)='Task2.6';
rt(2,1)='Value';
rt(2,2)=reaction_time_setting;
disp("Gain: "+i+" is done")
disp("Switches: "+Switch)
disp("Collisions:"+Collision)
disp("DNS: "+dns+" DNS1: "+dns1)
writematrix(Scenario_output,'KavyaChandrika_Vempalli.xls','Sheet','Sheet1','Range','D1:E4');
writematrix(Speed_Sampling,'KavyaChandrika_Vempalli.xls','Sheet','Sheet1','Range','D6:E8');
writematrix(Tuned_values,'KavyaChandrika_Vempalli.xls','Sheet','Sheet1','Range','D10:E14');
writematrix(Collision,'KavyaChandrika_Vempalli.xls','Sheet','Sheet1','Range','D16:E20');
writematrix(Switch,'KavyaChandrika_Vempalli.xls','Sheet','Sheet1','Range','D22:E24');
writematrix(rt,'KavyaChandrika_Vempalli.xls','Sheet','Sheet1','Range','D26:E27');

