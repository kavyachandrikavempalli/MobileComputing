%Reading Input From Channel
channel_id=2363661;
read_key='5BQFWKMKSHY7F44M';

user_data=thingSpeakRead(channel_id,'ReadKey',read_key,Fields=[1,2,3,4,5],NumMinutes=4); %[55	8	25	12	1]
%user_data=[55,8,25,12,1];
Hr_lcw=user_data(1,1);
Hr_hcw=user_data(1,1);
Rr_lcw=user_data(1,2);
Rr_hcw=user_data(1,2);
initSpeed=user_data(1,3);
dist_limit=user_data(1,4)*(-1);
Case=user_data(1,5);

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

%% Implementation
if Case==1
   if Hr_lcw<61
       Hr_lcw=61;
   end
   tr_lcw=(Hr_lcw/Rr_lcw)*0.01;
else
   if Hr_hcw<92
       Hr_hcw=92;
   end
   tr_hcw=(Hr_hcw/Rr_hcw)*0.01;
end

decelLim_lcw = -200;
decelLim_hcw = -150;
%load('data.mat');
        
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

%% Output to Channel
write_key='VU3IXS40QEL7RENH';
thingSpeakWrite(channel_id,'Fields',[6],'Values',Collision_stat,'WriteKey',write_key);
