Vincent Yang Read Me
FileZip:

Usage Instructions: 

to Zip: Call zip with in/out file name, method header: public static void zip(String inFileName, String outFileName)
to unzip: Call unzip with in/out file names, method header: public static void unzip(String inFile, String outFile)
to read file: Call readFile with the file name to be read (with extensions),
	method header: public static String readFile(String inputFile)
to write file: Call writeFile with the data to be written and the file name.
	method header: public static void writeFile(String data, String fileName)

Statistics:
Romeo: 
	Equality: true
	Compression Ratio: 0.45193322279167925
	Run Time: 2261 (milliseconds)

Banana:
	Equality: true
	Compression Ratio: 0.5896003343378131
	Run Time: 265 (milliseconds)

Chicken:
	Equality: true
	Compression Ratio: 0.6013303647508315
	Run Time: 187 (milliseconds)

numberData:
	Equality: true
	Compression Ratio: 0.5091645843045686
	RunTime: 2860 (milliseconds)

Kafka:
	Equality: true
	Compression Ratio: 0.46201196782421033
	Run Time: 766 (milliseconds)

SnackBar:
	Equality: true
	Compression Ratio: 0.5882080210184685
	Run Time: 94 (milliseconds)
