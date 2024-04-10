function [isSwitch,timeOfSwitch]=AdvisoryControl(velocityA,distAB)
    %simres= evalin('base', 'simout');
    newfus=readfis("KavyaChandrikaVempalli.fis");
    
    dist=evalin('base','simout.sxB.Data')-evalin('base','simout.sx1.Data');
    array_size=size(dist);
    vb=evalin('base','simout.vxB.Data')-40;
    ax=(-1)*evalin('base','simout.ax1.Data');
    tm=evalin('base','simout.ax1.Time');
    decelB = zeros(array_size(1),1);
    for i=1:array_size(1)
        decelB(i,1)=evalfis([ax(i,1),dist(i,1),vb(i,1)], newfus);
    end
    Cs=evalin('base','Case');
    if Cs==1
        decelLim=200;
    else
        decelLim=150;
    end
    isSwitch = 0;
     for i=1:array_size(1)
        if decelB(i,1)>(0.75*decelLim)
            isSwitch=1;
            disp("Switch to Human");
            %time of switch
            timeOfSwitch=tm(i,1);
            disp("i= "+i);
            break;
        else
            isSwitch=0;
            timeOfSwitch=-1;
            %Donot switch Scenario
        end
     end
end