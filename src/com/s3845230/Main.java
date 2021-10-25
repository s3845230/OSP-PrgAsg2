package com.s3845230;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            File VSFS = new File(args[1]);
            if (VSFS.createNewFile()) {
                BufferedWriter myWriter = new BufferedWriter(new FileWriter(args[1], true));
                myWriter.append("NOTES V1.0");
                myWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (args.length == 0) {
            System.exit(1);
        }

        if (args[0].equals("list")) {
            list(args[1]);
        } else if (args[0].equals("copyin")) {
            copyIn(args[3], args[1], args[2]);
        } else if (args[0].equals("copyout")) {
            copyOut(args[1], args[3], args[2]);
        } else if (args[0].equals("mkdir")) {
            recursiveDirectoryCreate(args[2], args[1]);
        } else if (args[0].equals("rm")) {
            removeInternalFile(args[2], args[1]);
        } else if (args[0].equals("rmdir")) {
            removeInternalDirectory(args[2], args[1]);
        } else if (args[0].equals("defrag")) {
            defrag(args[1]);
        } else if (args[0].equals("index")) {
            System.out.println("out of scope");
        } else if (args[0].equals("test")) {
            runTest(args[1]);
        }else {
            System.out.println("please input a real command");
        }
        System.exit(0);
    }

    public static void recursiveDirectoryCreate(String path, String VSFSloc) {
        File VSFS = new File(VSFSloc);
        if (path.length() == 0) {
            return;
        }
        try {
            int slashIndex = path.lastIndexOf("/"); //separates the path of the file to the directory it's from,
            if (slashIndex == path.length()-1) {
                path = path.substring(0,path.length()-1);
                slashIndex = path.lastIndexOf("/");
            }
            Scanner myReader = new Scanner(VSFS);       //to allow for checking if that directory exists
            boolean newDir = true;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.charAt(0) == '=') {
                    if (data.substring(1).equals(path+"/")) {
                        newDir = false; //checks if the directory holding the new directory has already been made
                    }
                }
            }
            if (newDir) {   //if this directory is new, recursively check this new directory to check if the directory that holds it is also new
                try {
                    if (countOccurrences(path, '/') > 0) {
                        recursiveDirectoryCreate(path.substring(0,Math.max(slashIndex, 0)), VSFSloc);
                    }
                    if (path.lastIndexOf("/") != path.length()-1) {
                        path = path + "/";
                    }
                    BufferedWriter myWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(VSFSloc, true), "UTF-8"));
                    myWriter.append(System.lineSeparator() + "=" + path); //create the new directory, creating from the lowest level first
                    myWriter.close();         //for example, =dir1/dir2/dir3 would create =dir1, then =dir1/dir2, then =dir1/dir2/dir3
                } catch (IOException e) {
                    System.exit(1);
                    System.err.println("Invalid VSFS");
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.exit(1);
            System.err.println("Invalid VSFS");
        }
    }

    public static void removeInternalFile (String path, String VSFSloc) {
        File VSFS = new File(VSFSloc);
        BufferedReader rd = null;
        BufferedWriter wt = null;
        boolean currentlyDeleting = false;

        try {
            rd = new BufferedReader(new InputStreamReader(new FileInputStream(VSFSloc), "UTF-8"));
            wt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("VSFStemp.notes"), "UTF-8"));
            //creates a temp VSFS.notes file to write into before replacing the old VSFS file with it
            String firstLine = rd.readLine();
            wt.write(firstLine);
            for (String line; (line = rd.readLine()) != null;) {
                //currentlyDeleting exists so that after deleting the target file path
                //the program continues to delete all it's contents until hitting the next file/director
                if (line.charAt(0) == '=' || line.charAt(0) == '@') {
                    currentlyDeleting = false;
                }
                if (line.substring(1).equals(path) || currentlyDeleting == true) {
                    line = "#" + line.substring(1);
                    currentlyDeleting = true;
                }
                wt.newLine();
                wt.write(line);
            }
            wt.close();
            rd.close();
            Files.move(Paths.get("VSFStemp.notes"), Paths.get("VSFStemp.notes").resolveSibling(VSFSloc),
                    StandardCopyOption.REPLACE_EXISTING); //replacement of old VSFS.notes file
        } catch (UnsupportedEncodingException e) {
            System.err.println("Invalid VSFS");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Invalid VSFS");
            System.exit(1);
        }
    }

    public static void removeInternalDirectory (String path, String VSFSloc){
        BufferedReader rd = null;
        BufferedWriter wt = null;
        boolean currentlyDeleting = false;
        if (path.charAt(path.length()-1) != '/') {
            path = path + "/"; //users can input dir, or dir/ - this ensures the program handles the same thing every time
        }
        try {
            rd = new BufferedReader(new InputStreamReader(new FileInputStream(VSFSloc), "UTF-8"));
            //temp VSFS file to make changes to and replace the original with
            wt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("VSFStemp.notes"), "UTF-8"));
            String firstLine = rd.readLine();
            wt.write(firstLine);
            for (String line; (line = rd.readLine()) != null;) {
                //currentlyDeleting exists so that after deleting the target file path
                //the program continues to delete all it's contents until hitting the next file/directory
                if (line.charAt(0) == '=' || line.charAt(0) == '@') {
                    currentlyDeleting = false;
                }   //if check for finding the target directory for removal, or anything in it
                if (line.substring(1, Math.min(path.length()+1,line.length())).equals(path) || currentlyDeleting == true) {
                    line = "#" + line.substring(1);
                    currentlyDeleting = true;
                }
                wt.newLine();
                wt.write(line);
            }
            wt.close();
            rd.close();
            Files.move(Paths.get("VSFStemp.notes"), Paths.get("VSFStemp.notes").resolveSibling(VSFSloc),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (UnsupportedEncodingException e) {
            System.err.println("Invalid VSFS");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Invalid VSFS");
            System.exit(1);
        }
    }

    public static void defrag (String VSFSloc){
        String currentPath = "init";
        String lastPath = "";
        boolean currentlyWriting = false;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream(VSFSloc), "UTF-8"));
            BufferedWriter wt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("VSFStemp.notes"), "UTF-8"));
            wt.write("Notes V1.0");
            while (currentPath.equals("") == false) {
                currentPath = "";
                for (String line; (line = rd.readLine()) != null; ) {
                    if (line.charAt(0) == '@' || line.charAt(0) == '=') {
                        if (lastPath.equals("")) {
                            if (currentPath.equals("")) {
                                currentPath = line;
                            //following two checks iterate through all the files/dirs and check which one is the 'shallowest'
                            //and also which is the alphabetically closest to a
                            } else if (countOccurrences(currentPath, '/') > countOccurrences(line, '/')) {
                                currentPath = line;
                            } else if (countOccurrences(currentPath, '/') == countOccurrences(line, '/') && line.compareToIgnoreCase(currentPath) < 0) {
                                currentPath = line;
                            }
                        //this just checks if the current line is 'better' than the last path, and if so,
                            // it means it's already been processed
                        } else if (countOccurrences(lastPath, '/') > countOccurrences(line, '/') || (countOccurrences(lastPath, '/') == countOccurrences(line, '/') && line.compareToIgnoreCase(lastPath) < 1)) {

                        } else {
                            //now that we know it's a candidate to list, we can check if it's better than the alternatives
                            if (currentPath.equals("")) {
                                currentPath = line;
                            } else if (countOccurrences(currentPath, '/') > countOccurrences(line, '/')) {
                                currentPath = line;
                            } else if (countOccurrences(currentPath, '/') == countOccurrences(line, '/') && line.compareToIgnoreCase(currentPath) < 0) {
                                currentPath = line;
                            }
                        }

                    }
                }
                rd.close();
                rd = new BufferedReader(new InputStreamReader(new FileInputStream(VSFSloc), "UTF-8"));
                //previous was just for finding the next file/dir, this part actually writes it over
                //currentlyWriting is to make sure the contents of the file also get printed, not just the '@note1' portion
                for (String line; (line = rd.readLine()) != null; ) {
                    if (line.charAt(0) == '@' || line.charAt(0) == '=' || line.charAt(0) == '#') {
                        currentlyWriting = false;
                    }
                    if (currentlyWriting == true) {
                        wt.newLine();
                        wt.write(line);
                    }
                    if (line.equals(currentPath)) {
                        wt.newLine();
                        wt.write(line);
                        currentlyWriting = true;
                    }
                }
                //set lastPath to currentPath, currentPath gets reset at start of loop
                //currentPath only stays blank if there are no files/dirs to port over
                lastPath = currentPath;
                rd.close();
                rd = new BufferedReader(new InputStreamReader(new FileInputStream(VSFSloc), "UTF-8"));
            }
            wt.close();
            rd.close();
            Files.move(Paths.get("VSFStemp.notes"), Paths.get("VSFStemp.notes").resolveSibling(VSFSloc),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (UnsupportedEncodingException e) {
            System.err.println("Invalid VSFS");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Invalid VSFS");
            System.exit(1);
        }
    }

    //helper class, mostly just to count how many '/' are in a file path to see how deep it is
    public static int countOccurrences(String string, char target)
    {
        int count = 0;
        if (string.charAt(string.length()-1) == '/') {
            count--;
        }
        for (int i=0; i < string.length(); i++)
        {
            if (string.charAt(i) == target)
            {
                count++;
            }
        }
        return count;
    }

    public static void copyIn(String path, String VSFSloc, String externalPath) {
        boolean error = false;
        int slashIndex = path.lastIndexOf("/");
        File VSFS = new File(VSFSloc);
        try {
            Scanner myReader = new Scanner(VSFS);
            if (slashIndex > -1) {
                error = true;
            }
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (slashIndex > -1) {
                    if (data.charAt(0) == '=') {
                        //if check to see if containing folder exists in VSFS
                        if (data.substring(1, Math.min(slashIndex + 1,data.length())).equals(path.substring(0, slashIndex))) {
                            error = false;
                        }
                    }
                }
                //if the exact file already exists, delete it so it can be replaced
                if (path.equals(data.substring(1))) {
                    removeInternalFile(path, VSFSloc);
                }
            }
            if (error) {
                System.err.println("Invalid VSFS");
                System.exit(1);
            }
            } catch (FileNotFoundException e) {
                System.err.println("Invalid VSFS");
                System.exit(1);
            }
        try {
            //section that encodes the file to Base64 and stores it in the VSFS. Limit of 252 for Base64 to ensure correct amount of bits
            byte[] fileBytes = Files.readAllBytes(Paths.get(externalPath));
            String fileString = Base64.getEncoder().encodeToString(fileBytes);
            fileString = fileString.substring(0, Math.min(fileString.length(), 252));
            BufferedWriter myWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(VSFSloc, true), "UTF-8"));

            myWriter.append(System.lineSeparator() + "@" + path);
            myWriter.append(System.lineSeparator() + " " + fileString);
            myWriter.close();
        } catch (IOException e) {
            System.err.println("Invalid VSFS");
            System.exit(1);
        }
    }

    public static void copyOut(String VSFSloc, String externalPath, String internalPath) {
        try {
            File VSFS = new File(VSFSloc);
            Scanner myReader = new Scanner(VSFS);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.charAt(0) == '@') {
                    //check to see if current iteration is the file searched for
                    if (("@" + internalPath).equals(data)) {
                        data = myReader.nextLine();
                        data = data.substring(1);
                        byte[] file = Base64.getDecoder().decode(data);
                        //write data to new file
                        try (FileOutputStream fos = new FileOutputStream(externalPath)) {
                            fos.write(file);
                        } catch (IOException e) {
                            System.err.println("Invalid VSFS");
                            System.exit(1);
                        }
                    }
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("Invalid VSFS");
            System.exit(1);
        }
    }

    public static void list(String VSFSloc) {
        try {
            File VSFS = new File(VSFSloc);
            PosixFileAttributes PosixAttr = Files.readAttributes(Paths.get(VSFSloc), PosixFileAttributes.class);
            BasicFileAttributes BasicAttr = Files.readAttributes(Paths.get(VSFSloc), BasicFileAttributes.class);
            SimpleDateFormat df = new SimpleDateFormat("LLL dd kk:kk");
            String dateCreated = df.format(BasicAttr.lastAccessTime().toMillis());
            Scanner myReader = new Scanner(VSFS);
            while (myReader.hasNextLine()) {
                String metadata = "";
                String data = myReader.nextLine();
                //if it's a file, start with -, if it's a dir, start with d
                if (data.charAt(0) == '@') {
                    metadata = "-";
                } else if (data.charAt(0) == '=') {
                    metadata = "d";
                }
                //checks to make sure deleted files or file contented don't get listed
                if (metadata.length() > 0) {
                    metadata = metadata + PosixFilePermissions.toString(PosixAttr.permissions()) + " ";
                    if (data.charAt(0) == '@') { //due to Base64 encoding, number of lines in file is always 1
                        metadata = metadata + " " + "  1" + " ";
                    } else if (data.charAt(0) == '=') {
                        //searches file for itself, eg, =dir4/ searches for =dir4/dir5/, and increments on successful find
                        Scanner myReader2 = new Scanner(VSFS);
                        int subDirCount = -1; //has to be -1, as search will always find itself
                        while (myReader2.hasNextLine()) {
                            String potentialSubDir = myReader2.nextLine();
                            if (data.equals(potentialSubDir.substring(0, Math.min(data.length(), potentialSubDir.length())))) {
                                subDirCount++;
                            }
                        }
                        String linkString = "" + subDirCount;
                        if (linkString.length() == 1) {
                            linkString = "  " + linkString;
                        }
                        if (linkString.length() == 2) {
                            linkString = " " + linkString;
                        }
                        metadata = metadata + " " + linkString + " ";
                        myReader2.close();
                    }
                    metadata = metadata + PosixAttr.owner().getName() + " ";
                    metadata = metadata + PosixAttr.group().getName() + " ";
                    if (data.charAt(0) == '@') { //due to Base64 encoding, number of lines in file is always 1
                        metadata = metadata + "1 ";
                    } else if (data.charAt(0) == '=') {
                        metadata = metadata + "0 ";
                    }
                    metadata = metadata + dateCreated + " ";
                    metadata = metadata + data.substring(1);
                    System.out.println(metadata);
                }

            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("Invalid VSFS");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Invalid VSFS");
            System.exit(1);
        }
    }

    public static void runTest (String VSFSloc) {
        String testFile1 = "testFile1.notes";
        String testFile2 = "testFile2.txt";
        String internalFile1 = "internalFile1";
        String internalFile2 = "internalFile2";
        String copyOutFile1 = "copyOutFile1.notes";
        String dir1 = "dir1/";
        String dir2 = "dir2";
        String dir3 = "dir2/dir3/";
        String dir5 = "dir4/dir5/";
        byte[] testFile1Bytes = testFile1.getBytes(StandardCharsets.UTF_8);
        try (FileOutputStream fos = new FileOutputStream(testFile1)) {
            fos.write(testFile1Bytes);
        } catch (IOException e) {
            System.exit(1);
            System.err.println("Invalid VSFS");
        }
        byte[] testFile2Bytes = testFile2.getBytes(StandardCharsets.UTF_8);
        try (FileOutputStream fos = new FileOutputStream(testFile2)) {
            fos.write(testFile2Bytes);
        } catch (IOException e) {
            System.exit(1);
            System.err.println("Invalid VSFS");
        }
        try {
            copyIn(internalFile1, VSFSloc, testFile1);
            copyIn(internalFile2, VSFSloc, testFile2);
            System.out.println("initial copyins complete");
            Files.copy(Paths.get(VSFSloc), Paths.get("stage001" + VSFSloc), StandardCopyOption.REPLACE_EXISTING);
            copyOut(VSFSloc, copyOutFile1, internalFile1);
            System.out.println("copyout complete");
            Files.copy(Paths.get(VSFSloc), Paths.get("stage002" + VSFSloc), StandardCopyOption.REPLACE_EXISTING);
            recursiveDirectoryCreate(dir3, VSFSloc);
            recursiveDirectoryCreate(dir1, VSFSloc);
            recursiveDirectoryCreate(dir5, VSFSloc);
            System.out.println("directory creations complete");
            Files.copy(Paths.get(VSFSloc), Paths.get("stage003" + VSFSloc), StandardCopyOption.REPLACE_EXISTING);
            copyIn(dir1 + internalFile1, VSFSloc, testFile1);
            copyIn(dir3 + internalFile2, VSFSloc, testFile1);
            System.out.println("copyins under directories complete");
            Files.copy(Paths.get(VSFSloc), Paths.get("stage004" + VSFSloc), StandardCopyOption.REPLACE_EXISTING);
            removeInternalFile(dir1 + internalFile1, VSFSloc);
            System.out.println("file deletion complete");
            Files.copy(Paths.get(VSFSloc), Paths.get("stage005" + VSFSloc), StandardCopyOption.REPLACE_EXISTING);
            removeInternalDirectory(dir2, VSFSloc);
            System.out.println("deletion of directory complete");
            Files.copy(Paths.get(VSFSloc), Paths.get("stage006" + VSFSloc), StandardCopyOption.REPLACE_EXISTING);
            defrag(VSFSloc);
            System.out.println("defrag complete");
            Files.copy(Paths.get(VSFSloc), Paths.get("stage007" + VSFSloc), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("VSFS list:");
            list(VSFSloc);
        } catch (IOException e) {
            System.err.println("Invalid VSFS");
            System.exit(1);
        }
    }
}
