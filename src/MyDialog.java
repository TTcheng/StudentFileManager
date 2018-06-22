import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyDialog extends JDialog implements ActionListener {
    //ui components
    private JButton nameBtn;
    private JButton stuIdBtn;
    private JButton deleteBtn;
    private Container contentPane;
    private JPanel buttonPanel;//学号、姓名按钮的面板
    private JPanel deletePanel;//组合框、删除按钮的面板
    private JPanel filesPanel;//文件显示面板
    private JComboBox<String> textComboBox;
    private JList<String> textJList;
    //data
    private Set<File> files = new HashSet<>();
    private Set<String> filePrefixNames = new HashSet<>();//文件名 不包含后缀名
    private Vector<String> fullFileNamesVector = new Vector<>();
    private File latestFile = null;
    private String latestFileName = null;

    public MyDialog() {
        super();
        //init ui components
        buttonPanel = new JPanel();
        deletePanel = new JPanel();
        filesPanel = new JPanel();

        nameBtn = new JButton("姓名");
        nameBtn.addActionListener(this);
        stuIdBtn = new JButton("学号");
        stuIdBtn.addActionListener(this);
        deleteBtn = new JButton("删除");
        deleteBtn.addActionListener(this);

        textComboBox = new JComboBox<>(fullFileNamesVector);
        textJList = new JList<>(fullFileNamesVector);
        //link ui components
        buttonPanel.add(nameBtn);
        buttonPanel.add(stuIdBtn);
        deletePanel.add(textComboBox);
        deletePanel.add(deleteBtn);
        filesPanel.add(textJList);

        contentPane = getContentPane();
        contentPane.add(buttonPanel);
        contentPane.add(deletePanel);
        contentPane.add(filesPanel);
        //set param
        setSize(1280, 720);
        setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));//contentPane的内容纵向排列
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//        pack();//大小自适应
    }

    private void refresh() {
        textComboBox.updateUI();
        textJList.updateUI();
    }

    private void deleteFile(String preFileName) {
        String fullName = preFileName+".dat";
        fullFileNamesVector.remove(fullName);
        filePrefixNames.remove(preFileName);
        for (File file : files) {
            if (file.getName().equals(fullName)) {
                files.remove(file);
                file.delete();
                break;
            }
        }
    }

    //随机产生一个12位的学号和5个小写字母的姓名，并生成文件
    private void createRandomStuFile() {
        //这里随机产生的名字和学号可能重复，虽然几率很小，但是不用担心，集合files和fileNames是不能有重复的项的，因此不会被重复添加
        String studentID = "20153106" + (int) (Math.random() * 10000);
        String studentName = StringUtil.getRandomName();
        latestFileName = studentID;
        File file = new File("src/", latestFileName + ".dat");
        if (file.exists()) {
            //已经存在该文件
            createRandomStuFile();//重新产生
        } else {
            filePrefixNames.add(latestFileName);
            String fullFileName = latestFileName + ".dat";
            fullFileNamesVector.add(fullFileName);
            files.add(file);
            latestFile = file;
            FileOutputStream fileOutputStream = null;
            try {
                file.createNewFile();
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write((studentName + studentID).getBytes());
                fileOutputStream.close();
            } catch (IOException e) {
                System.out.println("写文件失败！！");
                //写文件失败，撤销已经添加到列表的文件
                files.remove(file);
                file.delete();
                filePrefixNames.remove(latestFileName);
                fullFileNamesVector.remove(fullFileName);
                e.printStackTrace();
            }
        }
    }

    //为最新创建的文件产生3个副本并重命名
    private void copyFile() {
        int copyNum = 1;
        while (true) {
            //不存在同名文件
            if (!filePrefixNames.contains(latestFileName + copyNum)) {
                ExecutorService executorService = Executors.newFixedThreadPool(3);//三个线程的线程池
                //线程池执行三次文件复制
                executorService.execute(new RunnableCopy(copyNum));
                executorService.execute(new RunnableCopy(copyNum+1));
                executorService.execute(new RunnableCopy(copyNum+2));
                break;//退出循环
            }
            copyNum++;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == nameBtn) {
            createRandomStuFile();
        } else if (e.getSource() == stuIdBtn) {
            copyFile();
        } else if (e.getSource() == deleteBtn) {
            String fullFileName = (String) textComboBox.getSelectedItem();
            //这里可以保证选中的文件一定存在于
            deleteFile(fullFileName.split("\\.")[0]);
        }
        refresh();
//        printList();
    }
    //内部类，共享UI类的全局变量，方便操作
    public class RunnableCopy implements Runnable {
        private int copyNum;

        public RunnableCopy(int copyNum) {
            this.copyNum = copyNum;
        }

        @Override
        public void run() {
            String prefixName = latestFileName + "-" + copyNum;
            String newFileName = prefixName + ".dat";
            File newfile = new File("src/", newFileName);
            filePrefixNames.add(prefixName);
            fullFileNamesVector.add(newFileName);
            files.add(newfile);
            try {
                newfile.createNewFile();
                FileOutputStream out = new FileOutputStream(newfile);
                FileInputStream in = new FileInputStream(latestFile);
                byte[] b = new byte[1024];
                int n = 0;
                while ((n = in.read(b)) != -1) {
                    out.write(b, 0, n);
                }
                in.close();
                out.close();
            } catch (IOException e) {
                System.out.println("复制失败！！！");
                filePrefixNames.remove(prefixName);
                fullFileNamesVector.remove(newFileName);
                files.remove(newfile);
                newfile.delete();
                e.printStackTrace();
            }
        }
    }
    //test
//    private void printList(){
//        Iterator i1 = files.iterator();
//        Iterator i2 = fullFileNamesVector.iterator();
//        Iterator i3 = filePrefixNames.iterator();
//
//        while(i2.hasNext()){
//            System.out.println(i1.next());
//            System.out.println(i2.next());
//            System.out.println(i3.next());
//        }
//    }
}
