package com.s3845230;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println(args[0]);
        System.out.println("123");
        boolean error = false;
        try {
            File VSFS = new File(args[1]);
            if (VSFS.createNewFile()) {
                System.out.println("File is created!");
                BufferedWriter myWriter = new BufferedWriter(new FileWriter(args[1], true));
                myWriter.append("NOTES V1.0" + System.lineSeparator());
                myWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (args.length == 0) {
            System.exit(1);
        }

        if (args[0].equals("list")) {

        } else if (args[0].equals("copyin")) {
            System.out.println("reached copyin");
            //need checks to check if directory exists, if there is a '/' in name
            int inDir = args[3].lastIndexOf("/");
            if (inDir > -1) {
                String path = args[3].substring(0,inDir);
                File VSFS = new File(args[1]);
                try {
                    Scanner myReader = new Scanner(VSFS);
                    error = true;
                    while (myReader.hasNextLine()) {
                        String data = myReader.nextLine();
                        if (data.charAt(0) == '=') {
                            if (data.substring(1,inDir+1).equals(args[3].substring(0,inDir))) {
                                error = false;
                            }
                        }
                    }
                    if (error) {
                        System.out.println("Unknown location of internal file");
                        System.exit(1);
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }
            }
            try {
                byte[] fileBytes = Files.readAllBytes(Paths.get(args[2]));
                    System.out.println("attempting encoding");
                String fileString = Base64.getEncoder().encodeToString(fileBytes);
                    System.out.println("fileString: " + fileString);
                fileString = fileString.substring(0, Math.min(fileString.length(), 253));
//                BufferedWriter myWriter = new BufferedWriter(new FileWriter("VSFS.notes", true));
                BufferedWriter myWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("VSFS.notes", true), "UTF-8"));

                myWriter.append("@" + args[3] + System.lineSeparator());
                    System.out.println("attempting write of: " + "@" + args[3] + System.lineSeparator());
                myWriter.append(" " + fileString + System.lineSeparator()); //need 255 length limit?
                myWriter.close();
            } catch (IOException e) {
                throw new IllegalStateException("could not read file ", e);
            }
        } else if (args[0].equals("copyout")) {
            System.out.println("reached copyout");
            try {
                File VSFS = new File(args[1]);
                Scanner myReader = new Scanner(VSFS);
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    if (data.charAt(0) == '@') {
                        if (("@" + args[2]).equals(data)) {
                            data = myReader.nextLine();
                            data = data.substring(1);
                            byte[] file = Base64.getDecoder().decode(data);
                            try (FileOutputStream fos = new FileOutputStream(args[3])) {
                                fos.write(file);
                            } catch (IOException e) {
                                throw new IllegalStateException("could not copyout file ", e);
                            }
                        }
                    }
                }
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        } else if (args[0].equals("mkdir")) {

        } else if (args[0].equals("rm")) {

        } else if (args[0].equals("rmdir")) {

        } else if (args[0].equals("defrag")) {

        } else if (args[0].equals("index")) {

        }
    }
}
