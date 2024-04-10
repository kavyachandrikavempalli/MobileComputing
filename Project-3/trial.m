fid = fopen('file.json','w');
fprintf(fid,'%s',encoded);
fclose(fid);