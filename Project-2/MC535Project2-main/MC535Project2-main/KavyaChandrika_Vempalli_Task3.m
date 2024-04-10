%% Load data from Excel file
User_data=[1,80,14,16,6,95,26,21,14;2,65,15,13,4,71,21,14,5;3,61,14,17,8,92,23,26,16];
reaction_time_setting=11.5;

%% Extract HR and RR data for LCW and HCW
Hr_lcw = User_data(3, 2);
Rr_lcw = User_data(3, 4);
Rq_lcw=Hr_lcw/Rr_lcw;
tr_lcw=0.01*Rq_lcw*reaction_time_setting;

Hr_hcw = User_data(3, 6);
Rr_hcw = User_data(3, 8);
Rq_hcw=Hr_hcw/Rr_hcw;
tr_hcw=0.01*Rq_hcw*reaction_time_setting;

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

%% Iterating with different speeds trying different gains for lcw and hcw
for Case = 1:2
    for InitSpeed=20:2:60
        decelLim_lcw = -200;
        decelLim_hcw = -150;
        
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
        i=i+1;
    end
    if Case == 1
    disp("No Collision: " + dns + ", Switches: " + switch_count(1,1) + ", Collisions: "+dns1)
    finalGain_lcw = Gain;
    else
    disp("No Collision: " + dns + ", Switches: " + switch_count(2,1) + ", Collisions: "+dns1)
    finalGain_hcw = Gain;
    end
end

%% Output to Excel
FinalGain=string;
FinalGain(1,1)='Final Gain';
FinalGain(1,2)='Task3.1';
FinalGain(2,1)='Best gain - Low Cognitive Workload';
FinalGain(2,2)=finalGain_lcw;
FinalGain(3,1)='Best gain - High Cognitive Workload';
FinalGain(3,2)=finalGain_hcw;

writematrix(FinalGain,'KavyaChandrika_Vempalli.xls','Sheet','Sheet1','Range','A20:B22');
