% Data=readmatrix('p2.xlsx');
% a=size(Data);
% for i=1:a(1)
%     Gain(i,1)=Data(i,1);
% end
% for i=1:a(1)
%     Init_Speed(i,1)=Data(i,2);
% end
%excel=actxserver('excel.application');
%wkb=excel.Workbooks
%exlFile=open([docroot 'C:\Users\chand\OneDrive\Desktop\ASU Stuff\Subjects\Mobile_computing\Project-2\MC535Project2-main\MC535Project2-main\New.xlsx']);
%exlSheet1 = exlFile.Sheets.Item('Sheet1');


%Data=xlsread('New.xlsx','A2:E10');
%Data=xlsread('New.xlsx')
%hExcel = actxserver('excel.application'); 
%hExcel.Workbooks.Open([docroot 'C:\Users\chand\OneDrive\Desktop\ASU Stuff\Subjects\Mobile_computing\Project-2\MC535Project2-main\MC535Project2-main\p2.xlsx']);
%BtchData is the name of the sheet:
%data = hExcel.Worksheets.Item('Sheet1').UsedRange.Value; 

%h=readmatrix('p2.xlsx');
a=10;

%Average User
Hr_lcw=68.96;
Rr_lcw=15.52;
Hr_hcw=86.57;
Rr_hcw=20.62;

Rq_lcw=Hr_lcw/Rr_lcw;
Rq_hcw=Hr_hcw/Rr_hcw;

tr_hcw=0.01*Rq_hcw;
tr_lcw=0.01*Rq_lcw;
Data=readmatrix('p2.xlsx');
%Data1=typecast(Data,'int16');
writematrix(Data,'written.xlsx','FileType','spreadsheet');
% a=size(Data);
% for i=1:a(1)
%     Gain(i,1)=Data(i,1);
% end
% for i=1:a(1)
%     Init_Speed(i,1)=Data(i,2);
% end
decelLim_lcw = -200;
% decelLim_hcw = -150;
Gain=20000;
InitSpeed=25;
% [A,B,C,D,Kess, Kr, Ke, uD] = designControl(secureRand(),Gain);
% open_system('VehicleModelOnly.slx')

%set_param('VehicleModelOnly/VehicleKinematics/Saturation','LowerLimit',num2str(decelLim_lcw))
%set_param('VehicleModelOnly/VehicleKinematics/vx','InitialCondition',num2str(InitSpeed))
% simModel = sim('VehicleModelOnly.slx');
% print('Reached here');
% stopping_time=max(out.deceleration.time);

%Breaking control - What are the two inputs?, what are its units, distance,
%time etc

%When breaking control is removed and we want time ta from vehicle kinematics, 
%can we extract time, without looking at scope?

%predicted_stop_time - how to obtain this?
%collision_time=predicted_stop_time