import sys
sys.dont_write_bytecode = True
import PathFP_pb2

input_file = 'ENTER INPUT FILE NAME HERE.txt'

fps = PathFP_pb2.PathFP()
with open(input_file,'r') as finput:
    for line in finput:
        line = line.strip()
        attrs = line.split(',')
        fp=fps.record.add()
        fp.scanID = attrs[0]
        fp.timestamp = int(attrs[1])
        fp.latitude = float(attrs[2])
        fp.longitude = float(attrs[3])
        fp.building = attrs[4]
        fp.mac = attrs[5]
        fp.apname = attrs[6]
        fp.rss = int(attrs[7])

fpsstr=fps.SerializeToString()
with open('fps.pbf','wb') as fout:
    fout.write(fpsstr)

