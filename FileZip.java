/**
 * @author: Vincent Yang
 * @Date: 11/29/11
 * This class zips and unzips files using the LZW algorithm, and can also read and write from and to files.
 */
import java.io.*;
import java.util.*;
//everything going in/out must be in try/catch
public class FileZip
{
    //FIELDS
    private static int numDictBits = 15; //dict has 2^numDictBits possible symbols, less bits is faster
    //Romeo, Kafka, Banana, and Chicken only need 16 bits
    //numData needs 24 bits
    private static int maxCharCode = 32768/2; //unicode range taken
    private static int buffer;
    private static int writtenBits;
    private static int bitsLeft;
    private static BufferedOutputStream bos = null; //for zipping
    private static BufferedInputStream bis = null; //for unzipping
    private static HashMap<String, Integer> m_dictionary; //dictionary while zipping
    private static ArrayList<String> m_unZippingDictionary; //dictionary while unzipping
    private static int m_dictionarySize; //sizes for both dictionaries
    //METHODS

    /**
     * This method reads input files
     * @param filePath - input path
     * @return String - the content of the file
     */
    private String readFileAsString(String filePath)
    {
        FileInputStream reader = null;
        String result = null;

        try
        {
            byte[] buffer = new byte[(int) new File(filePath).length()];
            reader = new FileInputStream(filePath);
            reader.read(buffer);
            result = new String(buffer, "UTF-8");
        }
        catch (IOException ex)
        {
            System.out.println("File cannot be read.");
            return null;
        }
        finally
        {
            try
            {
                if (reader!= null)
                {
                    reader.close();
                }
            }
            catch(IOException ex)
            {
                System.out.println("FIle cannot be closed." );
                return null;
            }
        }
        return "";
    }
    /**
     *
     */
    public static String readFile(String inputFile)
    {
        FileReader reader = null;
        String returningString = "";
        try
        {
            reader = new FileReader(inputFile);
            Scanner in = new Scanner(reader);
            while(in.hasNext())
            {
                returningString += in.nextLine() + System.getProperty("line.separator");
            }
        }
        catch (IOException exception)
        {
            System.out.println("Error Reading File");
        }
        finally
        {
            if(reader != null)
            {
                try
                {
                    reader.close();
                }
                catch(IOException exception)
                {
                    System.out.println(exception.getMessage());
                }
            }
        }
        return returningString;
    } //reads files in from current folder
    public static void writeFile(String data, String fileName)
    {
        FileWriter f = null;
        try
        {
            f = new FileWriter(fileName);
            f.write(data);
            f.close();

        }
        catch (FileNotFoundException i)
        {
            System.out.println("File is not found " + i);
        }
        catch (IOException i)
        {
            System.out.println("the readFile method has an eXception" +  i);
        }
    } //writes files out to current folder
    /**
     * Scans the file for whether or not there are too many symbols for the current amount of bits to hold.
     * @param writeToFile - whether or not the data should be written to the file
     * @param fileString - the file to be written in
     * @return true if the bits are enough, false if the bits are not enough
     */
    private static boolean scanSymbol (String fileString, boolean writeToFile)
    {
        m_dictionary = initDict();
        String prevCharacter = "";
        if (writeToFile)
        {
            // the first byte indicates how many bits are being used
            System.out.println(numDictBits + " bits are being used");
            writeByte(numDictBits);
        }
        for (int i = 0; i < fileString.length(); i++)
        {
            String currentCharacter = fileString.substring(i, i+1);
            // guard against single character not in the dictionary
            if (m_dictionary.get(currentCharacter)==null)
            {
                m_dictionary.put(currentCharacter, m_dictionarySize);
                System.out.println("new word X: " + currentCharacter+", code="+m_dictionarySize);
                m_dictionarySize++;
            }
            String entry = prevCharacter.concat(currentCharacter);
            if (m_dictionary.containsKey(entry))
            {
                prevCharacter = entry;
                //add the last part to the part before
            }
            else
            {
                //if the entry is not yet in the dictionary
                m_dictionary.put(entry, m_dictionarySize);


                m_dictionarySize++; //expand dictionary

                //add the character in the dict to output
                if (writeToFile)
                {
                    write(m_dictionary.get(prevCharacter), numDictBits); //write it out
                }
                else
                {

                    if (m_dictionary.get(prevCharacter) < 0 || m_dictionary.get(prevCharacter) >= (1 < numDictBits)) //
                    {
                        System.out.println("The value is " + m_dictionary.get(prevCharacter));<
                            return false;
                    }
                }
                prevCharacter = currentCharacter; //look for the next group
            }
        }
        //output

        if (writeToFile)
        {
            if (!prevCharacter.equals("")) // ADDS single characters that were not added with ASCII
            {
                write(m_dictionary.get(prevCharacter), numDictBits);
            }
            try
            {
                clearBuffer(); // write out any remaining bits
                bos.flush();
                bos.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return true; //there are enough bits to hold the number of symbols
    }
    //ZIPPING METHODS
    public static void zip(String inFileName, String outFileName)
    {

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(outFileName);
            bos = new BufferedOutputStream (fos);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        String fileString = readFile(inFileName);

        while (!scanSymbol(fileString, false))
        {
            System.out.println(numDictBits + " bits are not enough");
            numDictBits++;
        }
        System.out.println("Using " + numDictBits + " bits provides enough");
        scanSymbol(fileString, true); //


    } //zips input files
    public static HashMap<String, Integer> initDict()
    {
        HashMap<String,Integer> dictionary = new HashMap<String,Integer>(/*size*/ maxCharCode*2);
        Integer code;
        for (int i = 0; i < maxCharCode; i++)
        {
            code = new Integer(i);
            dictionary.put("" + (char)i, code);

        }
        m_dictionarySize = maxCharCode;
        return dictionary;
    } //initializes the dictionary from Zip

    public static void unzip(String inFile, String outFile) //unzips input files
    {

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(inFile);
            bis = new BufferedInputStream(fis);
        }
        catch (FileNotFoundException f)
        {
            f.printStackTrace();
        }
        catch(SecurityException s)
        {
            s.printStackTrace();
        }
        //convert binary to integer array/string
        completeBuffer();

        m_dictionarySize = maxCharCode;

        m_unZippingDictionary = new ArrayList<String>(65535); //starts at s^16 spots
        char[] symbol = new char[1];
        for (int i = 0; i < maxCharCode; i++)
        {
            symbol[0] = (char) i;
            m_unZippingDictionary.add(new String (symbol));
        }
        numDictBits = readInt(8);
        System.out.println(numDictBits + " bits are being read in unzip");
        String currentCharacter = "" + (char)readInt(numDictBits); //get the first code
        StringBuilder output = new StringBuilder(currentCharacter);
        int i = 0;
        while ((i = readInt(numDictBits))!= -1)
        {
            String entry = "";
            if (m_dictionarySize > i) //if the dictionary already has the current character, if the max size of the array is greater than the current
            {
                entry = entry.concat(m_unZippingDictionary.get(i)); //get the character at i
            }
            else if (i == m_dictionarySize)
            {
                // new code
                entry = entry.concat(currentCharacter + currentCharacter.charAt(0));
            }
            else
            {
                throw new IllegalArgumentException("Bad compressed i " + i);
            }
            output = output.append(entry);

            //add to dict
            m_unZippingDictionary.add(currentCharacter.concat(entry.substring(0, 1)));
            m_dictionarySize++;
            //m_unZippingDictionary.put(m_dictionarySize++, output + entry.charAt(0));
            currentCharacter = entry;
        }
        writeFile(output.toString(), outFile);
    }

    //BIT COMPRESSION METHODS
    private static void writeOutBit(boolean bit) //writes out bits
    {
        buffer <<= 1; //buffer gets pushed one to the left
        if (bit) //buffer has last 0, if bit is true, set as 1. else, leave as is
        {
            buffer |= 1;
        }
        writtenBits++;
        if (writtenBits == 8)
        {
            //print and reset the buffer
            clearBuffer();
        }
    }
    private static void clearBuffer()
    {
        if (writtenBits == 0)
            return; //nothing happens
        else if (writtenBits > 0)
        {
            buffer <<= (8 - writtenBits);
        }
        try
        {
            bos.write(buffer);
        }
        catch (IOException e)
        {
            System.out.println("error in clearBuffer()");
            e.printStackTrace();
        }
        //reset all
        writtenBits = 0;
        buffer = 0;
    }
    private static void flush()
    {
        clearBuffer();
        try
        {
            bos.flush();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    private static void write(int code, int numBits)
    {
        if (numBits == 32)
        {
            write(code);
        }
        else if (numBits < 1 || numBits > 32)
        {
            throw new RuntimeException("You cannot have less than one or more than 32 bits = " + numBits);
        }
        else if (code < 0 || code >= (1 << numBits))
        {
            throw new RuntimeException(numBits + " bits are not enough to hold " + code);
        }
        for (int i = 0; i < numBits; i++) //print out the output
        {
            boolean bit = ((code >>> (numBits - i - 1)) & 1) == 1; //write out each bit
            writeOutBit(bit);
        }
    }
    private static void write(int i)
    {
        writeByte((i >>> 24) & 0xff); //hex number (two bytes), x is 15 (all bits in byte are 1), move current byte to the right and mask othersb
        writeByte((i >>> 16) & 0xff);
        writeByte((i >>> 8) & 0xff);
        writeByte((i >>> 0) & 0xff);
    }
    private static void writeByte(int x)
    {
        assert x >= 0 && x < 256;

        if (writtenBits == 0)
        {
            try
            {
                bos.write(x);
            }
            catch (IOException e)
            {
                e.printStackTrace();

            }
            return;
        }
        for (int i = 0; i < 8; i++)
        {
            boolean b = ((x >>> (8 - i - 1)) & 1 ) == 1;
            writeOutBit(b);
        }
    }
    private static int readInt(int numBitsPerInt)
    {
        if (numBitsPerInt < 1 || numBitsPerInt > 32)
        {
            throw new RuntimeException ("readInt(int i) cannot take 0/negative or greater than 32, bits.");
        }
        int result = 0;
        try
        {

            for (int k = 0; k < numBitsPerInt; k++)
            {
                result <<= 1; //push everything one to the left
                boolean bit = readBoolean();
                if (bit)
                {
                    result |= 1; //if bit is true, set the rightmost bit to one
                }
            }
        }
        catch (RuntimeException e)
        {
            return -1; //reached end of file
        }
        return result;
    }
    private static boolean readBoolean()
    {
        if (bitsLeft == 0 /*if it is empty*/)
            throw new RuntimeException("Reading from empty input stream");
        bitsLeft--;
        boolean bit = ((buffer >> bitsLeft) & 1) == 1;
        if (bitsLeft == 0)
        {
            completeBuffer();
        }
        return bit;
    }
    private static void completeBuffer()//fill in the byte buffer
    {
        try
        {
            buffer = bis.read();
            if (buffer == -1)
                bitsLeft = 0;
            else
                bitsLeft = 8;
        }
        catch (IOException e)
        {
            System.out.println("End of the file cannot be read");
            buffer = -1;
            bitsLeft = 0;
        }
    }
}
