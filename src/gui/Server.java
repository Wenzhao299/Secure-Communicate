package gui;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

@SuppressWarnings("serial")
public class Server extends JFrame{
	JPanel p1 = new JPanel();
	JPanel p2 = new JPanel();
	JPanel p3 = new JPanel();
	JScrollPane js=new JScrollPane(p2);
	Label lbPort = new Label("Port");
	//Label lbCount = new Label("Counts of Client");
	Label lbSay = new Label("Say");
	Label lbToPort = new Label("ToPort");
	TextField tfPort = new TextField(25);
	//TextField tfCount = new TextField(5);
	TextField tfSay = new TextField(25);
	TextField tfToPort = new TextField(5);
	TextArea taMsg = new TextArea(15,50);
	Button btnStart = new Button("Start");
	Button btnSay = new Button("Say");

	Socket[] socket_server = new Socket[10];
	ServerSocket[] server = new ServerSocket[10];
	Map<Integer, Integer> port_map = new HashMap<Integer, Integer>();
	int port_index = 0;
	static int server_port = 0;
	
	public Server() {
		this.add(p1,"North");
		p1.setPreferredSize(new Dimension(600, 65));
		Border borPort = BorderFactory.createTitledBorder("服务器设置");
        p1.setBorder(borPort);
		/*p1.add(lbCount); p1.add(tfCount);*/  p1.add(lbPort);	p1.add(tfPort);	p1.add(btnStart);
		this.add(p2,"Center");
		Border borMsg = BorderFactory.createTitledBorder("消息框");
        p2.setBorder(borMsg);
		p2.add(taMsg);
		//分别设置水平和垂直滚动条总是出现 
		js.setHorizontalScrollBarPolicy( 
		JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 
		js.setVerticalScrollBarPolicy( 
		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 
		this.add(p3,"South");
		p3.setPreferredSize(new Dimension(600, 65));
		Border borSay = BorderFactory.createTitledBorder("发送消息");
		p3.setBorder(borSay);
		p3.add(lbToPort); p3.add(tfToPort); p3.add(lbSay); p3.add(tfSay); p3.add(btnSay); //p3.add(lbInstructions);
		btnStart.addActionListener(new startListener());
		btnSay.addActionListener(new sayListener());
		//Client.btnSay.addActionListener(new clientsayListener());
		/*WindowAdapter window = new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		};
		this.addWindowListener(window);*/
	}
	
	//获取16进制随机数
	public static String randomHexString(int len) {
		try {
			StringBuffer result = new StringBuffer();
			for(int i=0;i<len;i++) {
				result.append(Integer.toHexString(new Random().nextInt(16)));
			}
			return result.toString().toUpperCase();	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();	
		}
		return null;	
	}

	//startListener
	class startListener implements ActionListener {
		public void actionPerformed(ActionEvent e1) {
			Runnable serverRunnable = new Runnable() {
				@Override
				public void run() {
					int port = Integer.parseInt(tfPort.getText());
					//int count = Integer.parseInt(tfCount.getText());
					try {
						server[port_index] = new ServerSocket(port);
						taMsg.append("Server starting...\n");
						socket_server[port_index] = server[port_index].accept();
						taMsg.append("Client(port:" + port + ") connected...\n");
						port_map.put(port,port_index);
						port_index++;
					}catch(Exception e2) {
						System.out.println(e2);
						taMsg.append("The port is not available!\n");
					}
					clientsay(port);
				}
			};
			Thread serverThread = new Thread(serverRunnable);
			serverThread.start();
		}
	}

	//sayListener
	class sayListener implements ActionListener {
		Base64.Encoder encoder = Base64.getEncoder();
		//String serverrandomkey = randomHexString(16);
		private final String key = randomHexString(16);
		//private final String key = "A1B2C3D4E5F60708";	//DES/AES
		String ServerPriKey = RSAUtil.ServerkeyMap.get(1);
		String ClientPubKey = RSAUtil.ClientkeyMap.get(0);
		//String ClientPriKey = RSAUtil.ClientkeyMap.get(1);
		//String ServerPubKey = RSAUtil.ServerkeyMap.get(0);

		public void actionPerformed(ActionEvent e) {
			/*System.out.println("服务器：");
			System.out.println("ServerPriKey:" + ServerPriKey);
			System.out.println("ClientPubKey:" + ClientPubKey);*/
			//System.out.println("ClientPriKey:" + ClientPriKey);
			//System.out.println("ServerPubKey:" + ServerPubKey);

			//Runnable sayRunnable = new Runnable() {
				//@Override
				//public void run() {
					try {
						int port = Integer.parseInt(tfToPort.getText());
						OutputStream out = socket_server[port_map.get(port)].getOutputStream();		//得到客户端的输出流，为了向客户端输出数据
						PrintWriter writer = new PrintWriter(out,true);
						String serversay = tfSay.getText();

						//签名
						byte[] serversignature = RSAUtil.sign(serversay, ServerPriKey);
						String ServerSignature = encoder.encodeToString(serversignature);

						//加密
						String encryptData = AESUtil.encrypt(serversay, key);	//DES/AES
						//String encryptData = ThreeDESUtil.encrypt(serversay, keyiv, key);	//ThreeDES
						String ClientPubKeyEncrypt = RSAUtil.encrypt(key, ClientPubKey); //使用客户端公钥对密钥种子加密

						writer.println(server_port + " " + ServerSignature + encryptData + ClientPubKeyEncrypt);
						taMsg.append("Server(to port:" + port +")" +  serversay + "\n");
						tfSay.setText(null);
						System.out.println("服务器发送的消息：" + serversay);
						System.out.println("加密后的消息：" + encryptData + "\n");
					}catch(Exception e1) {
						System.out.println(e1);
						taMsg.append("Communicate failed...\n");
					}
				//}
			//};
			//Thread sayThread = new Thread(sayRunnable);
			//sayThread.start();
		}
	}

	public void client2client(String fromPort, String toPort, String message){
		Base64.Encoder encoder = Base64.getEncoder();
		//String serverrandomkey = randomHexString(16);
		String key = randomHexString(16);
		//private final String key = "A1B2C3D4E5F60708";	//DES/AES
		String ServerPriKey = RSAUtil.ServerkeyMap.get(1);
		String ClientPubKey = RSAUtil.ClientkeyMap.get(0);
		try {
			OutputStream out = socket_server[port_map.get(Integer.parseInt(toPort))].getOutputStream();		//得到客户端的输出流，为了向客户端输出数据
			PrintWriter writer = new PrintWriter(out,true);

			//签名
			byte[] serversignature = RSAUtil.sign(message, ServerPriKey);
			String ServerSignature = encoder.encodeToString(serversignature);

			//加密
			String encryptData = AESUtil.encrypt(message, key);	//DES/AES
			//String encryptData = ThreeDESUtil.encrypt(serversay, keyiv, key);	//ThreeDES
			String ClientPubKeyEncrypt = RSAUtil.encrypt(key, ClientPubKey);

			writer.println(fromPort + " " + ServerSignature + encryptData + ClientPubKeyEncrypt);
			taMsg.append("client(port:"+ fromPort +") to client(port:" + toPort + "):" +  message + "\n");
			System.out.println("client(port:"+ fromPort +") to client(port:" + toPort + "):" + message);
			System.out.println("加密后的消息：" + encryptData + "\n");
		}catch(Exception e1) {
			System.out.println(e1);
			taMsg.append("Communicate failed...\n");
		}
	}

	public void clientsay(int port) {
		//int count = Integer.parseInt(tfCount.getText());
		Base64.Decoder decoder = Base64.getDecoder();
		String ServerPriKey = RSAUtil.ServerkeyMap.get(1);
		String ClientPubKey = RSAUtil.ClientkeyMap.get(0);

		while(true){
				//System.out.println(port);
				try {
					InputStream in = socket_server[port_map.get(port)].getInputStream();		//得到客户端的输入流，为了得到客户端传来的数据
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					String clientsay = null;
					while(true){
						clientsay = reader.readLine();
						if(!clientsay.trim().equals("")) {
							String[] str = clientsay.split(" ");
							String toPort = str[0];
							String message = str[1];

							//解密
							String clientrandomkey = RSAUtil.decrypt(message.substring(message.length()-172), ServerPriKey);
							String decryptData = AESUtil.decrypt(message.substring(172, message.length()-172), clientrandomkey);	//DES/AES
							//String decryptData = ThreeDESUtil.decrypt(message, keyiv, key);	//ThreeDES

							if(toPort.equals(Integer.toString(server_port))) {
								taMsg.append("Client(port:" + port + "):" + decryptData + "\n");
								System.out.println("服务器接收到的消息：" + message);
								System.out.println("解密后的消息：" + decryptData);
								//验证
								String ClientSignature = message.substring(0, 172);
								byte[] clientsignature = decoder.decode(ClientSignature.getBytes());
								RSAUtil.verify(decryptData, ClientPubKey, clientsignature);
							}
							else {
								//验证
								String ClientSignature = message.substring(0, 172);
								byte[] clientsignature = decoder.decode(ClientSignature.getBytes());
								RSAUtil.verify(decryptData, ClientPubKey, clientsignature);
								client2client(Integer.toString(port), toPort, decryptData);
							}
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			//}
		}
	}

	/*public static void main(String[] args) {
		Server frameServer = new Server();
		frameServer.setTitle("Server");
		frameServer.setSize(600, 460);
		frameServer.setLocation(360, 300);
		frameServer.setVisible(true);
	}*/
}

