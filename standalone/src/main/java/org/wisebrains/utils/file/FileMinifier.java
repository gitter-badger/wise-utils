package org.wisebrains.utils.file;

import org.apache.commons.io.FileUtils;
import org.wisebrains.utils.model.FileWithExtention;
import org.wisebrains.utils.operation.OperationType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by eXo Platform MEA on 26/05/14.
 *
 * @author <a href="mailto:mtrabelsi@exoplatform.com">Marwen Trabelsi</a>
 *
 * This utility artifact can be used either to shrink a file by removing repeatable lines or to
 * extract some lines to another file.
 * The counter is launched once the search key is found. It limits to either a stop token if specified
 * Or when reaching some range of lines when the flag "withLinesCount" is specified.
 *
 * Use below commands after build:
 * To minify a file based on lines count:
 *  java -jar standalone-x.y.z.jar OPERATION WITH_LINES_COUNT FILE_NAME DESTINATION_FILE_NAME(optional) SEARCHED_KEY LINE_NUMBERS
 * To minify a file based on some stop key:
 *  java -jar standalone-x.y.z.jar OPERATION WITHOUT_LINES_COUNT FILE_NAME DESTINATION_FILE_NAME(optional) SEARCHED_KEY STOP_KEY_WORD
 *
 */
public class FileMinifier
{
  private String fileFromPath;
  private String fileToPath;
  private String searchKey;
  private int linesToCount;
  private String stopKey;

  public FileMinifier(String fileFromPath, String fileToPath, String searchKey, int linesToCount)
  {
    this.fileFromPath = fileFromPath;
    this.fileToPath = fileToPath;
    this.searchKey = searchKey;
    this.linesToCount = linesToCount;
  }

  public FileMinifier(String fileFromPath, String fileToPath, String searchKey, String stopKey)
  {
    this.fileFromPath = fileFromPath;
    this.fileToPath = fileToPath;
    this.searchKey = searchKey;
    this.stopKey = stopKey;
  }

  public FileMinifier(String fileFromPath, String searchKey, int linesToCount)
  {
    this(fileFromPath,
        new FileWithExtention(fileFromPath, "-minified").getFilePath(),
        searchKey,
        linesToCount);
  }

  public FileMinifier(String fileFromPath, String searchKey, String stopKey)
  {
    this(fileFromPath,
        new FileWithExtention(fileFromPath, "-minified").getFilePath(),
        searchKey,
        stopKey);
  }

  public static void main(String[] args)
  {

    FileMinifier fileMinifier;
    OperationType operation = OperationType.valueOf(args[0].toUpperCase());

    switch (operation)
    {
      case REMOVE:
      {
        if (args.length > 5)
        {
          if (args[1].equals("withLinesCount"))
          {
            fileMinifier = new FileMinifier(args[2], args[3], args[4], Integer.parseInt(args[5]));
            fileMinifier.removeFromFileWithLineCount();
          } else
          {
            fileMinifier = new FileMinifier(args[2], args[3], args[4], args[5]);
            fileMinifier.removeFromFileWithKeyStop();
          }
        } else
        {
          if (args[1].equals("withLinesCount"))
          {
            fileMinifier = new FileMinifier(args[2], args[3], Integer.parseInt(args[4]));
            fileMinifier.removeFromFileWithLineCount();
          } else
          {
            fileMinifier = new FileMinifier(args[2], args[3], args[4]);
            fileMinifier.removeFromFileWithKeyStop();
          }
        }
        break;
      }
      case EXTRACT:
      {
        if (args.length > 5)
        {
          if (args[1].equals("withLinesCount"))
          {
            fileMinifier = new FileMinifier(args[2], args[3], args[4], Integer.parseInt(args[5]));
            fileMinifier.extractToFileWithLineCount();
          } else
          {
            fileMinifier = new FileMinifier(args[2], args[3], args[4], args[5]);
            fileMinifier.extractToFileWithKeyStop();
          }
        } else
        {
          if (args[1].equals("withLinesCount"))
          {
            fileMinifier = new FileMinifier(args[2], args[3], Integer.parseInt(args[4]));
            fileMinifier.extractToFileWithLineCount();
          } else
          {
            fileMinifier = new FileMinifier(args[2], args[3], args[4]);
            fileMinifier.extractToFileWithKeyStop();
          }
        }
        break;
      }
    }
  }

  public File removeFromFileWithLineCount()
  {
    File sourceFile = new File(fileFromPath);
    File destinationFile = new File(fileToPath);
    BufferedReader br = null;
    BufferedWriter bw = null;

    try
    {
      br = new BufferedReader(new FileReader(sourceFile));
      bw = new BufferedWriter(new FileWriter(destinationFile));
      Pattern pattern = Pattern.compile(Pattern.quote(searchKey), Pattern.CASE_INSENSITIVE);
      String currentLine;
      while ((currentLine = br.readLine()) != null)
      {
        if (pattern.matcher(currentLine).find())
        {
          int index = 1;
          while (index < linesToCount)
          {
            br.readLine();
            index++;
          }
        } else
        {
          bw.write(currentLine);
          bw.newLine();
        }
      }
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
    } catch (IOException e)
    {
      e.printStackTrace();
    } finally
    {
      try
      {
        if (br != null)
        {
          br.close();
        }
        if (bw != null)
        {
          bw.close();
        }
      } catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    return destinationFile;
  }

  public File extractToFileWithLineCount()
  {
    File sourceFile = new File(fileFromPath);
    File destinationFile = new File(fileToPath);
    BufferedReader br = null;

    try
    {
      br = new BufferedReader(new FileReader(sourceFile));
      Pattern pattern = Pattern.compile(Pattern.quote(searchKey), Pattern.CASE_INSENSITIVE);
      String currentLine;
//      System.out.println(searchKey); //TODO: Add log trace instead of console out statements
      while ((currentLine = br.readLine()) != null)
      {
//        System.out.println(currentLine.contains(searchKey)); //TODO: Add log trace instead of console out statements
        if (pattern.matcher(currentLine).find())
        {
          for (int i = 0; i < linesToCount; i++)
          {
            FileUtils.writeStringToFile(destinationFile, currentLine.concat("\n"), "UTF-8", true);
            currentLine = br.readLine();
          }
        }
      }
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
    } catch (IOException e)
    {
      e.printStackTrace();
    } finally
    {
      try
      {
        if (br != null)
        {
          br.close();
        }
      } catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    return destinationFile;
  }

  public File removeFromFileWithKeyStop()
  {
    File sourceFile = new File(fileFromPath);
    File destinationFile = new File(fileToPath);
    BufferedReader br = null;
    BufferedWriter bw = null;

    try
    {
      br = new BufferedReader(new FileReader(sourceFile));
      bw = new BufferedWriter(new FileWriter(destinationFile));
      Pattern pattern = Pattern.compile(Pattern.quote(searchKey), Pattern.CASE_INSENSITIVE);
      Pattern stopPattern = Pattern.compile(Pattern.quote(searchKey), Pattern.CASE_INSENSITIVE);
      String currentLine;
      while ((currentLine = br.readLine()) != null)
      {
        if (pattern.matcher(currentLine).find())
        {
          do
          {
            currentLine = br.readLine();
          }
          while (!((currentLine != null) && (stopPattern.matcher(currentLine).find())));
        } else
        {
          bw.write(currentLine);
          bw.newLine();
        }
      }
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
    } catch (IOException e)
    {
      e.printStackTrace();
    } finally
    {
      try
      {
        if (br != null)
        {
          br.close();
        }
        if (bw != null)
        {
          bw.close();
        }
      } catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    return destinationFile;
  }

  public File extractToFileWithKeyStop()
  {
    File sourceFile = new File(fileFromPath);
    File destinationFile = new File(fileToPath);
    BufferedReader br = null;
    boolean quitIteration;

    try
    {
      br = new BufferedReader(new FileReader(sourceFile));
      Pattern pattern = Pattern.compile(Pattern.quote(searchKey), Pattern.CASE_INSENSITIVE);
      Pattern stopPattern = Pattern.compile(Pattern.quote(stopKey), Pattern.CASE_INSENSITIVE);
      String currentLine;
      outerWhile:
      while ((currentLine = br.readLine()) != null)
      {
        if ((pattern.matcher(currentLine).find()))
        {
          quitIteration = false;
          do
          {
            FileUtils.writeStringToFile(destinationFile, currentLine.concat("\n"), "UTF-8", true);
            currentLine = br.readLine();
            if (currentLine == null)
              break outerWhile;
            if (stopPattern.matcher(currentLine).find())
            {
              FileUtils.writeStringToFile(destinationFile, currentLine.concat("\n"), "UTF-8", true);
              quitIteration = true;
            }
          }
          while (!quitIteration);
        }
      }
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
    } catch (IOException e)
    {
      e.printStackTrace();
    } finally
    {
      try
      {
        if (br != null)
        {
          br.close();
        }
      } catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    return destinationFile;
  }
}
