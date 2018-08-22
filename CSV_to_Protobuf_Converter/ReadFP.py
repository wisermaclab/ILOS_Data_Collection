import sys
sys.dont_write_bytecode = True
import PathFP_pb2

fps = PathFP_pb2.PathFP()

with open('fps.pbf','rb') as finput:
    fps.ParseFromString(finput.read())

counter = 0
for fp in fps.record:
    counter +=1
    print(fp.scanID,fp.timestamp,fp.latitude,fp.longitude,fp.building,fp.mac,fp.apname,fp.rss)
print('Total Number of Records:',counter)
