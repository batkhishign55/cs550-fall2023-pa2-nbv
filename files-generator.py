import os
import sys

if len(sys.argv)!=2:
    print("Peer not specified")
    sys.exit(1)
print(os.getcwd())

peer = sys.argv[1]

filesDirectory = os.getcwd()+"/bin/files/"

if not os.path.exists(filesDirectory):
    os.makedirs(filesDirectory)

def generate_files(bytetype, size_in_bytes, files_count, file_type):
    for i in range(0, files_count):
        extension = ".txt" if file_type=="text" else ".bin"
        filename = bytetype+"0"*(6-len(str(i)))+str(i)+"_"+peer+extension
        file_path = os.path.join(filesDirectory, filename)

        with open(file_path, 'wb') as file:
            content = bytes(os.urandom(size_in_bytes))
            file.write(content)

generate_files("10KB_", 10*1024, 10, "text")
# generate_files("100MB_", 100*1024*1024, 10, "text")

