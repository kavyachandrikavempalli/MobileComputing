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

load("MemberDecel200.mat");
Case=1;
%%
if Case==1
   HumanReactionTime=(Hr_lcw/Rr_lcw)*0.01;
else
   HumanReactionTime=(Hr_hcw/Rr_hcw)*0.01;
end
initSpeedA=25;
initSpeedB=30;

load_system("LaneMaintainSystem3Car.slx");
set_param('LaneMaintainSystem3Car/VehicleKinematics/Saturation','LowerLimit',num2str(decelLim));
set_param('LaneMaintainSystem3Car/VehicleKinematics/vx','InitialCondition',num2str(initSpeedB));
set_param('LaneMaintainSystem3Car/CARA/VehicleKinematics/Saturation','LowerLimit',num2str(decelLim));
set_param('LaneMaintainSystem3Car/CARA/VehicleKinematics/vx','InitialCondition',num2str(initSpeedA));
simout=sim("LaneMaintainSystem3Car.slx");

va=simout.vx1.Data;
sb=simout.sxB.Data;
sab=simout.sxB.Data-simout.sx1.Data;
vb=simout.vxB.Data-40;
sa=simout.sx1.Data;
