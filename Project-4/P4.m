% %% Load data from Excel file
% User_data=[1,80,14,16,6,95,26,21,14;2,65,15,13,4,71,21,14,5;3,61,14,17,8,92,23,26,16];
% User_3=User_data(3,:);
% 
% %% Extract HR and RR data for LCW and HCW
% lcw_hr = User_data(3, [2, 3]);
% lcw_rr = User_data(3, [4, 5]);
% hcw_hr = User_data(3, [6, 7]);
% hcw_rr = User_data(3, [8, 9]);
% 
% %% Generate random samples for user 3
% lcw_hr_val = normrnd(lcw_hr(1, 1), lcw_hr(1, 2),50,1);
% lcw_rr_val = normrnd(lcw_rr(1, 1), lcw_rr(1, 2),50,1);
% hcw_hr_val = normrnd(hcw_hr(1, 1), hcw_hr(1, 2),50,1);
% hcw_rr_val = normrnd(hcw_rr(1, 1), hcw_rr(1, 2),50,1);
% 
% %% Calculate the mean value
% Hr_lcw = mean(lcw_hr_val);
% Rr_lcw = mean(lcw_rr_val);
% 
% Hr_hcw = mean(hcw_hr_val);
% Rr_hcw = mean(hcw_rr_val);

%load("MemberDecel200.mat");
user_data=readmatrix("android_out.xlsx");
channel_id=2363661;
read_key='5BQFWKMKSHY7F44M';
write_key='VU3IXS40QEL7RENH';
resultdata=thingSpeakRead(channel_id,'ReadKey',read_key,Fields=[1,2,3,4,5]);
Hr_lcw=user_data(1,2);
Hr_hcw=user_data(1,2);
Rr_lcw=user_data(2,2);
Rr_hcw=user_data(2,2);
initSpeed=user_data(3,2);
dist_limit=user_data(4,2);

Case=user_data(5,2);

%% Initializations
Update = 0;
Control = 0;
Switch = 0;
Crash = 0;
Collision_count=zeros(2,1);
switch_count=zeros(2,1);
p = 1;
lcw_count=0;
hcw_count=0;
dns=0;
dns1=0;
Speed_Sample_count=0;
finalGain_lcw=0;
finalGain_hcw=0;
json_out="";
Collision_stat=0;

%%
if Case==1
   tr_lcw=(Hr_lcw/Rr_lcw)*0.01;
else
   tr_hcw=(Hr_hcw/Rr_hcw)*0.01;
end
%initSpeed=25;
initSpeedB=30;

% load_system("LaneMaintainSystem.slx");
% set_param('LaneMaintainSystem/VehicleKinematics/Saturation','LowerLimit',num2str(decelLim));
% set_param('LaneMaintainSystem/VehicleKinematics/vx','InitialCondition',num2str(initSpeedB));
% set_param('LaneMaintainSystem/CARA/VehicleKinematics/Saturation','LowerLimit',num2str(decelLim));
% set_param('LaneMaintainSystem/CARA/VehicleKinematics/vx','InitialCondition',num2str(initSpeedA));
% simout=sim("LaneMaintainSystem3Car.slx");

% va=simout.vx1.Data;
% sb=simout.sxB.Data;
% sab=simout.sxB.Data-simout.sx1.Data;
% vb=simout.vxB.Data-40;
% sa=simout.sx1.Data;
decelLim_lcw = -200;
decelLim_hcw = -150;
load('workspace.mat');
        
if Case == 1
  decelLim = decelLim_lcw;
  TR = tr_lcw;
  % After trying several values for gain, 110000 was the best
  % gain for lcw
  Gain = 110000;
  lcw_count=lcw_count+1;
  disp("Case: LCW");
else
  decelLim = decelLim_hcw;
  TR = tr_hcw;
  % After trying several values for gain, 110000 was the best
  % gain for hcw
  Gain = 200000;
  hcw_count=hcw_count+1;
  disp("Case: HCW");
end
[A,B,C,D,Kess, Kr, Ke, uD] = designControl(secureRand(),Gain);
load_system("LaneMaintainSystem.slx");
        
set_param('LaneMaintainSystem/VehicleKinematics/Saturation','LowerLimit',num2str(decelLim))
set_param('LaneMaintainSystem/VehicleKinematics/vx','InitialCondition',num2str(initSpeed))
set_param('LaneMaintainSystem/VehicleKinematics/sx','InitialCondition',num2str(dist_limit))

simModel1 = sim('LaneMaintainSystem.slx');
distance=simModel1.sx1.data;
ss=size(distance,1);
time=simModel1.sx1.Time;
for z=1:ss
    if distance(z,1)>0 && Update==0
        Update=1;
        %time_of_collision(p,1)=time(z-1,1);
        %tc=time_of_collision(p,1);
        %p=p+1;
        tc=time(z-1,1);
        disp("Time of collision: "+tc);
    end
end
if max(simModel1.sx1.data) < 0
    disp("Do Not Switch: No Collision.");
    json_out="Relax";
    dns=dns+1;
    Collision_stat=1;
else
    load_system("HumanActionModel.slx");
    set_param('HumanActionModel/VehicleKinematics/Saturation','LowerLimit',num2str(1.1*decelLim))
    set_param('HumanActionModel/VehicleKinematics/vx','InitialCondition',num2str(initSpeed))
    set_param('HumanActionModel/Step','Time',num2str(TR))
    set_param('HumanActionModel/Step','After',num2str(1.1*decelLim))
    simModel2 = sim('HumanActionModel.slx');

    if max(simModel2.deceleration.time) < tc %hstop<tstop
        disp("Switch to Human.");
        switch_count(Case,1) = switch_count(Case,1) + 1;
        json_out="Switch";
        Collision_stat=2;
    else
        disp("Do Not switch: Collision.");
        Collision_count(Case,1)=Collision_count(Case,1)+1;
        dns1=dns1+1;
        json_out="Collision";
        Collision_stat=3;
    end
end
encoded=jsonencode(json_out);
fid = fopen('matlab_out.json','w');
fprintf(fid,'{\n');
road_condition=jsonencode("road_condition");
fprintf(fid,"\t%s: [\n",road_condition);
fprintf(fid,"\t\t{\n");
ans1=jsonencode("ans");
fprintf(fid,"\t\t\t%s: [\n",ans1);
fprintf(fid,"\t\t\t\t{\n");
text=jsonencode("text");
fprintf(fid,"\t\t\t\t\t%s:%s,\n",text,encoded);
stat=jsonencode("status");
oo=jsonencode("OK");
fprintf(fid,"\t\t\t\t\t%s : %s\n",stat,oo);
fprintf(fid,"\t\t\t\t}\n");
fprintf(fid,"\t\t\t]\n");
fprintf(fid,"\t\t}\n");
fprintf(fid,"\t],\n");
fprintf(fid,"\t%s: %s\n",stat,oo);
fprintf(fid,"}\n");
fclose(fid);


thingSpeakWrite(channel_id,'Fields',[6],'Values',Collision_stat,'WriteKey',write_key);
% Speed_Sample_count=Speed_Sample_count+1;
% %i=i+1;
% if Case == 1
%     disp("No Collision: " + dns + ", Switches: " + switch_count(1,1) + ", Collisions: "+dns1)
%     finalGain_lcw = Gain;
% else
%     disp("No Collision: " + dns + ", Switches: " + switch_count(2,1) + ", Collisions: "+dns1)
%     finalGain_hcw = Gain;
% end