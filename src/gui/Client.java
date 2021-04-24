package gui;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.Random;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

@SuppressWarnings("serial")
public class Client extends JFrame{
	JPanel p1 = new JPanel();
	JPanel p2 = new JPanel();
	JPanel p3 = new JPanel();
	JScrollPane js=new JScrollPane(p2);
	Label lbServerIP = new Label("ServerIP");
	Label lbServerPort = new Label("ServerPort");
	Label lbSay = new Label("Say");
	Label lbToPort = new Label("ToPort");
	TextField tfServerIP = new TextField(15);
	TextField tfServerPort = new TextField(15);
	TextField tfSay = new TextField(25);
	TextField tfToPort = new TextField(5);
	TextArea taMsg = new TextArea(15,50);
	Button btnConnect = new Button("Connect");
	Button btnSay = new Button("Say");
	Socket socket_client = null;
	
	public Client(){
		this.add(p1,"North");
		p1.setPreferredSize(new Dimension(600, 65));
		Border borPort = BorderFactory.createTitledBorder("客户机设置");
        p1.setBorder(borPort);
		p1.add(lbServerIP);	p1.add(tfServerIP);	p1.add(lbServerPort); p1.add(tfServerPort);	p1.add(btnConnect);
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
		btnConnect.addActionListener(new clientListener());
		btnSay.addActionListener(new sayListener());
		//Server.btnSay.addActionListener(new serversayListener());
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
			System.out.println(e);
		}
		return null;
	}

	//clientListener
	class clientListener implements ActionListener {
		public void actionPerformed(ActionEvent e1) {
			Runnable clientRunnable = new Runnable() {
				@Override
				public void run(){
					taMsg.append("Connect to server...\n");
					String serverip = String.valueOf(tfServerIP.getText());
					int serverport = Integer.parseInt(tfServerPort.getText());
					try {
						socket_client = new Socket(serverip,serverport);
						taMsg.append("Server connected...\n");
					}catch(Exception e2) {
						System.out.println(e2);
						taMsg.append("Connect failed...\n");
					}
					serversay();
				}
			};
			Thread clientThread = new Thread(clientRunnable);
			clientThread.start();
		}
	}
	
	//sayListener
	class sayListener implements ActionListener {
		Base64.Encoder encoder = Base64.getEncoder();
		String clientrandomkey = randomHexString(16);
		private final String key = clientrandomkey;
		//private final String key = "A1B2C3D4E5F60708";	//DES/AES
		//private final String key = "A1B2C3D4E5F60708A1B2C3D4E5F60708A1B2C3D4E5F60708A1B2C3D4E5F60708";	//ThreeDES
        //byte[] keyiv = { 1, 2, 3, 4, 5, 6, 7, 8 };	//ThreeDES
		String ClientPriKey = RSAUtil.ClientkeyMap.get(1);
		String ServerPubKey = RSAUtil.ServerkeyMap.get(0);
		//String ServerPriKey = RSAUtil.ServerkeyMap.get(1);
		//String ClientPubKey = RSAUtil.ClientkeyMap.get(0);

		@Override
		public void actionPerformed(ActionEvent e) {
			/*System.out.println("客户机：");
			System.out.println("ClientPriKey:" + ClientPriKey);
			System.out.println("ServerPubKey:" + ServerPubKey);*/
			//System.out.println("ServerPriKey:" + ServerPriKey);
			//System.out.println("ClientPubKey:" + ClientPubKey);

			//Runnable sayRunnable = new Runnable() {
				//@Override
				//public void run() {
					try {
						int port = Integer.parseInt(tfToPort.getText());
						OutputStream out = socket_client.getOutputStream();		//获取服务端的输出流，为了向服务端输出数据
						PrintWriter writer = new PrintWriter(out,true);
						String clientsay = tfSay.getText();
							
						//签名
						byte[] clientsignature = RSAUtil.sign(clientsay, ClientPriKey);
						String ClientSignature = encoder.encodeToString(clientsignature);
							
						//加密
						String encryptData = AESUtil.encrypt(clientsay, key);	//DES
						//String encryptData = ThreeDESUtil.encrypt(clientsay, keyiv, key);	//ThreeDES
						String ServerPubKeyEncrypt = RSAUtil.encrypt(key, ServerPubKey);
							
						writer.println(port + " " + ClientSignature + encryptData + ServerPubKeyEncrypt);
						taMsg.append("Client:" + clientsay + "\n");
						tfSay.setText(null);
						System.out.println("客户机发送的消息：" + clientsay);
						System.out.println("加密后的消息：" + encryptData + "\n");
					}catch(Exception e1) {
						System.out.println(e1);
						taMsg.append("Communicate failed...");
					}
				//}
			//};
			//Thread sayThread = new Thread(sayRunnable);
			//sayThread.start();
		}
	}

	public void serversay(){
		Base64.Decoder decoder = Base64.getDecoder();
		String ClientPriKey = RSAUtil.ClientkeyMap.get(1);
		String ServerPubKey = RSAUtil.ServerkeyMap.get(0);
		try {
			InputStream in = socket_client.getInputStream();		//获取服务端的输入流，为了获取服务端输入的数据
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String serversay = null;
			while(true){
				serversay = reader.readLine();
				if(!serversay.trim().equals("")) {
					String[] str = serversay.split(" ");
					String fromPort = str[0];
					String message = str[1];

					//解密
					String serverrandomkey = RSAUtil.decrypt(message.substring(message.length()-172), ClientPriKey);
					String decryptData = AESUtil.decrypt(message.substring(172, message.length()-172), serverrandomkey);	//DES/AES
					//String decryptData = ThreeDESUtil.decrypt(message, keyiv, key);	//ThreeDES

					if(fromPort.equals(Integer.toString(Server.server_port))){
						taMsg.append("Server:" + decryptData + "\n");
						System.out.println("客户机接收到的消息：" + message);
						System.out.println("解密后的消息：" + decryptData);
						//验证
						String ServerSignature = message.substring(0, 172);
						byte[] serversignature = decoder.decode(ServerSignature.getBytes());
						RSAUtil.verify(decryptData, ServerPubKey, serversignature);
					}
					else {
						taMsg.append("client(port:" + fromPort + "):" + decryptData + "\n");
						System.out.println("客户机接收到的消息：" + message);
						System.out.println("解密后的消息：" + decryptData);
						//验证
						String ServerSignature = message.substring(0, 172);
						byte[] serversignature = decoder.decode(ServerSignature.getBytes());
						RSAUtil.verify(decryptData, ServerPubKey, serversignature);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/*public static void main(String[] args) {
		Client frameClient = new Client();
		frameClient.setTitle("Client");
		frameClient.setSize(600, 460);
		frameClient.setLocation(960, 300);
		frameClient.setVisible(true);
	}*/
}

