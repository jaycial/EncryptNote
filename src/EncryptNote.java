import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Calendar;
import javax.swing.*;
public class EncryptNote{
	
	/********声明一些全局变量***********/
	public static String Key;		//定义全局变量Key，用来存储密钥
	public static int KeyNumber=0;			//定义全局变量KeyNumber，用来存储密钥的摘要值
	public static String FileName;			//定义全局变量FileName，用来存储日记名
	public static boolean IsOpenFile=false;		//用于判断自己定义的openFile()函数是否被调用（此处避免了一个BUG）
	
	/***************main主函数*****************/
	public static void main(String[] args) {
		
		/***********图形用户界面设计******/
		JFrame f=new JFrame();				
		f.setBounds(100,100,500,700);				//设定窗口位置及大小
		f.setLayout(null);							//设定为空布局
		f.setTitle("加密日记本");						//设定窗口名称
		JButton b1, b2,b3;							//声明三个按钮，分别用来加密，解密和清屏
		final JTextArea text=new JTextArea(2,1);	//声明一个文本区域，用来接收用户输入的字符
		JScrollPane p = new JScrollPane(text);		//为文本框添加滚动条				
			/*为按钮分别命名*/
		b1=new JButton("解密读取");
		b2=new JButton("加密保存");
		b3=new JButton("清除文本");
		
		/*在窗口种添加按钮和文本框，并分别设定其在窗口中的位置*/
		f.getContentPane().add(b1);
		b1.setBounds(3,3,100,20);
		f.getContentPane().add(b2);
		b2.setBounds(106,3,100,20);
		f.getContentPane().add(b3);
		b3.setBounds(209,3,100,20);
		f.getContentPane().add(p);
		p.setBounds(3,28,494,670);
		
		f.setVisible(true);										//设置窗口可见
		f.setResizable(false);									//设置不可调整窗口大小	
		f.setLocationRelativeTo(null);                 	   		//让窗体居中显示
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		
		
		/***************监听事件的响应***************/
		
		  /**为b1（解密读取文件）按钮添加单击事件响应**/
		b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	String fileName;		//声明局部变量fileName用来保存用户输入的文档名称
            	fileName = JOptionPane.showInputDialog("请输入要阅读日记的保存时间（如2010-1-1）："); 
            	if(fileName!=null)		//确定用户输入了文件名（此处避免了一个BUG）
            	{
            	/*判断用户输入的文档名是否存在（*注：函数searchFaile(str)用于在D盘目录下查找文件名为str的txt文档。）*/
            	while(searchFile(fileName)==false)		//如果不存在该日记文件
				{
            		JOptionPane.showMessageDialog(null, "不错在该日记文件", "错误", JOptionPane.ERROR_MESSAGE); 		//提示用户不存在该文档
            		fileName = JOptionPane.showInputDialog("请输入要阅读日记的保存时间（如2010-1-1）："); 				//提示用户重新输入文档名称
				}
            	//跳出上方的while循环后（即在D盘下找到了用户制定的文件后）提示用户输入与之对应的密钥
            	Key = JOptionPane.showInputDialog("请输入与该篇日记对应的密钥："); 				
            	if(Key!=null)
            	{
            	/*计算用户输入密钥的摘要（密钥摘要为密钥各个字符的Ascii码相加值）*/
            	for(int i=0;i<Key.length();i++)				
				{
					int s=Key.charAt(i);
					KeyNumber=KeyNumber+s;
				}
            	
				/*判断用户输入的密钥及密钥摘要是否与文档中存储的一致（*注:函数keyTest（key，keynumber）用于判断用key、keynumber是否与用户先前储存再文档种的密钥、密钥摘要一致）*/
				while(keyTest(Key,KeyNumber)==false)
				{
					KeyNumber=0;		//将全局变量的密钥摘要重置为0
					JOptionPane.showMessageDialog(null, "密钥不匹配", "错误", JOptionPane.ERROR_MESSAGE); 		//提示用户密钥不匹配
					Key = JOptionPane.showInputDialog("请输入与该篇日记对应的密钥：");								//提示用户重新输入密钥
					
					/*计算用户输入密钥的摘要*/
					for(int i=0;i<Key.length();i++)
					{
						int s=Key.charAt(i);
						KeyNumber=KeyNumber+s;
					}
				}
				text.setText("");			//清空文本区域	
				String readStr;
				readStr=read();				//读取目标文档种的字符串，解密并存于字符串readStr中
				text.setText(readStr);		//将readStr种的内容显示在图形界面的文本区域中
            	}
            }
            }
        });
		
		 /**为b2（加密保存文件）按钮添加单击事件响应**/
		b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	String s=text.getText();		//获取文本区域中的内容并赋值给字符串s
            	openFile();						//调用openFile（）函数，此函数用于在D盘根目录下创建名为当前日期的文本文档，并将用户输入的的密钥与密钥摘要保存在文档的前两行。
            	if(IsOpenFile==true)			//判断opeFile()函数是否被调用，此处避免了一个BUG
            	{
            	write(s);						//将s加密写入刚刚创建的文本文档中，write函数为加密书写函数
            	JOptionPane.showMessageDialog(null, "加密文件已成功保存于："+FileName, "提示", JOptionPane.INFORMATION_MESSAGE);		//提示用户文档加密保存成功，并提示用户文件保存的路径及文件名
            	text.setText("");				//清空图形用户界面中的文本区域
            	KeyNumber=0;					//将全局变量的密钥摘要重置为0
            	IsOpenFile=false;				//将判断openFile()函数是否被调用的布尔判断值重置成false
            	}
            }
        });
		
		 /**为b3（加密保存文件）按钮添加单击事件响应**/
		b3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	text.setText("");		//清空图形用户界面中的文本区域
            }
        });
	}
	
	/******************下面为定义的一些功能函数***********************/
	
		/*定义openFile函数，用于在目标目录下创建日记文本文档,并存储密钥*/
		public static void openFile()
		{
			int year,month,day;		//用于储存当前的日期
			
			/********获取当前日期（年月日）作为日记名********/
			Calendar cal=Calendar.getInstance();
			year=cal.get(Calendar.YEAR);    
			month=cal.get(Calendar.MONTH)+1;    //由于月份从0开始，故加1才能正确表示当前月份
			day=cal.get(Calendar.DATE);  
			FileName="d:\\"+year+"-"+month+"-"+day+".txt";		//存为日记文档名
			
			/*********获取用户自定义的密钥**********/
			String key;			//用于临时存储用户输入的密钥
			int keyNumber=0;		//用于临时存储密钥的摘要值
			boolean keyLegal=false;	//判断密钥格式是否合法
			
			key = JOptionPane.showInputDialog("请输入用于加密的密钥（由5个英文字母组成）："); 		//提示用户输入密钥
			if(key!=null)
			{
			/**判断用户输入的密钥是否合法**/
			//确保密钥长度为5
			if(key.length()==5)
			{
				int i=0;
				for(i=0;i<key.length();i++)
				{
					//确保密钥中只存在字母
					if(!(('A'<=(key.charAt(i))&&(key.charAt(i))<='Z')||('a'<=(key.charAt(i))&&(key.charAt(i))<='z')))
					{
						break;
					}
				}
				if(i==5)
				{
					keyLegal=true;
				}
			}
			
			/*若密钥格式不合法，提示用户重新输入*/
			while(keyLegal==false)
			{
				JOptionPane.showMessageDialog(null, "输入的密钥不合法", "错误", JOptionPane.ERROR_MESSAGE); 		//提示用户输入错误
				key = JOptionPane.showInputDialog("请输入用于加密的密钥（由5个英文字母组成）："); 					//提示用户重新输入
				
				/*判断密钥是否合法*/
				if(key.length()==5)	//确保输入的字符长度为5
				{
					int i=0;
					for(i=0;i<key.length();i++)
					{
						//确保密钥中只有字母
						if(!(('A'<=(key.charAt(i))&&(key.charAt(i))<='Z')||('a'<=(key.charAt(i))&&(key.charAt(i))<='z')))	//确保输入的字符都是字母
						{
							break;
						}
					}
					if(i==5)
					{
						keyLegal=true;
					}
				}
			}
			
			//将密钥由局部变量传给全局变量。
			Key=key;	
			
			//计算密钥的摘要值，并赋值给keyNumber
			for(int i=0;i<key.length();i++)
			{
				int s=key.charAt(i);
				keyNumber=keyNumber+s;
			}
			//将密钥摘要由局部变量传递给全局变量
			KeyNumber=keyNumber;
			
			/**在目标目录下创建以当前日期为文件名的日记文档**/
			try
			{
				File f=new File(FileName);
				FileWriter f_w=new FileWriter(f,false);
				BufferedWriter bf_w=new BufferedWriter(f_w);
				//先将密钥加密并存于日记文档的第一行
				bf_w.write(encrypt(Key));
				bf_w.newLine();
				//再将密钥的摘要加密并存于日记文档的第二行
				bf_w.write(encrypt(Integer.toString(keyNumber)));
				bf_w.newLine();
				bf_w.flush();
				bf_w.close();
				f_w.close();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			//openFile函数被成功调用，IsOpenFile的值变为：true
			IsOpenFile=true;
			}
		}
		
		//定义write函数，用于将字符串str加密写入文件里
			public static void write(String str)
			{
				//将字符串str加密，赋值给s
				String s=encrypt(str);
				try{
				File f=new File(FileName);
				FileWriter f_w=new FileWriter(f,true);
				BufferedWriter bf_w=new BufferedWriter(f_w);
				//向目标文档里写入加密过的字符串s
				bf_w.write(s);
				bf_w.newLine();
				bf_w.flush();
				bf_w.close();
				f_w.close();
				}catch(Exception ex){
				ex.printStackTrace();
				}
			}
		
		//定义read函数，用于将日记文档中的加密文字解密读出
		public static String read()
		{
			String s,s1,s2;
			StringBuffer readStr=new StringBuffer();
			try{
				File f=new File(FileName);
				FileReader f_r=new FileReader(f);
				BufferedReader bf_r=new BufferedReader(f_r);
				//将目标文档中的字符串解密并读出
				int i=1;
				while((s1=bf_r.readLine())!=null)
				{ 
					//解密字符串
					s2=decrypt(s1);
					//输出字符串
					readStr.append(s2);
					//将密钥和密钥摘要下面各自加上换行符
					if(i<=2)
					{
						readStr.append("\r\n");
						i++;
					}			
				}
				bf_r.close();
				f_r.close();
				}catch(Exception ex){
				System.out.println("error!");
				}
			s=readStr.toString();
			return s;
		}
		
		//定义searchFile函数，用于查找目标目录下是否有名为str的文本文档
		public static boolean searchFile(String str)
		{	
			String fileName;		//存储文档名字
			boolean fileExist=true;		//判断文档是否存在
			fileName="d:\\"+str+".txt";
			try{
				File f=new File(fileName);
				FileReader f_r=new FileReader(f);
				BufferedReader bf_r=new BufferedReader(f_r);
				bf_r.close();
				f_r.close();
				//若存在此文件，将文件名存于全局变量FileName中
				FileName=fileName;
				}catch(Exception ex){
				fileExist=false;
				}
			return fileExist;
		}
		
		//定义encrypt函数，函数根据密钥对源字符串进行位移
		public static String encrypt(String str)
		{
			String s,s0,key;
			int a,m;
			char key0;
			StringBuffer keyBuf=new StringBuffer();
			StringBuffer enStr=new StringBuffer();
			//将用户输入的密钥倒序保存在key中
			for(int i=4;i>=0;i--)
			{
				key0=Key.charAt(i);
				keyBuf.append(key0);
			}
			key=keyBuf.toString();
			//加密
			for(int i=0;i<str.length();i++)
			{
				a=i%5;//用于控制循环密钥中的元素
				//字符串中每个字符的ascii逐个与密钥中逐个字符的ascii码相加
				m=(str.charAt(i)+key.charAt(a));
				//为了便于区分每个字符的ascii码位数，每加密一个字符，后加空格。
				s0=m+" ";
				enStr.append(s0);
			}
			s=enStr.toString();
			return s;
		}
		
		//定义decrypt函数，根据输入的密钥对加密字符串进行解密
		public static String decrypt(String str)
		{
			String s,key;
			int a,m,j=0;
			char c,key0;
			StringBuffer keyBuf=new StringBuffer();
			StringBuffer s0=new StringBuffer();
			StringBuffer deStr=new StringBuffer();
			//将用户输入的密钥倒序保存在key中
			for(int i=4;i>=0;i--)
			{
				key0=Key.charAt(i);
				keyBuf.append(key0);
			}
			key=keyBuf.toString();
			
			//解密
			for(int i=0;i<str.length();i++)
			{
				//读出连续的加密Ascii码（空格为结束）
				if(str.charAt(i)!=' ')
				{
					s0.append(str.charAt(i));
				}
				//解密刚读出的加密Ascii码
				else
				{
					a=j%5;
					m=Integer.parseInt(s0.toString());
					c=(char)(m-key.charAt(a));
					deStr.append(c);
					s0=new StringBuffer();	//重置s0
					j++;		//j控制密钥循环，i控制加密字符串的逐个读取。
				}
			}
			s=deStr.toString();
			return s;
		}
		
		//定义keyTest函数,用于核对密钥
		public static boolean keyTest(String key,int keyNumber)
		{
			boolean isMatched=false;
			String s1,s2;
			int m=0;
			//打开用户输入日期对应的文档
			try{
				File f=new File(FileName);
				FileReader f_r=new FileReader(f);
				BufferedReader bf_r=new BufferedReader(f_r);
				//读目标文档里的第一行（加密过的密钥）字符串
				s1=bf_r.readLine();
				//读目标文档里第二行（加密过的密钥摘要）字符串
				s2=bf_r.readLine();
				//对第一行字符（加密过的密钥）串解密
				s1=decrypt(s1);
				//对第二行字符串（加密过的密钥摘要）解密
				s2=decrypt(s2);
				//将密钥摘要转换成int型
				m=Integer.parseInt(s2);
				//判断解密出来的密钥与摘要是否与用户输入的一致
				if(s1.equals(Key)&&(m==KeyNumber))
				{
					isMatched=true;
				}
				bf_r.close();
				f_r.close();
				}catch(Exception ex){
				System.out.println("error!");
				}
			return isMatched;
		}
	}

