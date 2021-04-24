/*
package gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Communicate extends Frame {
	JPanel jp = new JPanel();
	Button btnServer = new Button("Start Server");
	Button btnClient = new Button("Start Client");
	int client_count = 0;
	int server_count = 0;

	public Communicate() {
		this.add(jp,"Center");
		jp.setPreferredSize(new Dimension(600, 65));
		Border borPort = BorderFactory.createTitledBorder("选择启动项");
		jp.setBorder(borPort);
		jp.add(btnServer); jp.add(btnClient);
		//btnServer.addActionListener(new start_server());
		//btnClient.addActionListener(new start_client());
		Login.btnLoginServer.addActionListener(new start_server());
		Login.btnLoginClient.addActionListener(new start_client());
		WindowAdapter window = new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		};
		this.addWindowListener(window);
	}

*/
/*	public void Login(){
		Login frameLogin = new Login();
		frameLogin.setTitle("Login");
		frameLogin.setSize(400, 235);
		frameLogin.setLocation(760, 300);
		frameLogin.setVisible(true);
	}*//*


	class start_server implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(Login.status) {
				if(server_count==1){
					JOptionPane.showMessageDialog(null, "只能启动一次Server！");
				}else{
					Server frameServer = new Server();
					frameServer.setTitle("Server");
					frameServer.setSize(600, 460);
					frameServer.setLocation(360, 300);
					frameServer.setVisible(true);
					server_count++;
				}
			}
			else {
				JOptionPane.showMessageDialog(null, "登录失败！");
			}
		}
	}

	class start_client implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(Login.status) {
				if(client_count==10){
					JOptionPane.showMessageDialog(null, "最多支持10个Client！");
				}else{
					Client frameClient = new Client();
					frameClient.setTitle("Client" + ++client_count);
					frameClient.setSize(600, 460);
					frameClient.setLocation(960, 300);
					frameClient.setVisible(true);
				}
			}
			else {
				JOptionPane.showMessageDialog(null, "登录失败！");
			}
		}
	}

	public static void main(String[] args) {
		*/
/*Communicate frameCommunicate = new Communicate();
		frameCommunicate.setTitle("Control");
		frameCommunicate.setSize(300, 100);
		frameCommunicate.setLocation(810, 150);
		frameCommunicate.setVisible(true);*//*

		Login frameLogin = new Login();
		frameLogin.setTitle("Login");
		frameLogin.setSize(400, 235);
		frameLogin.setLocation(760, 300);
		frameLogin.setVisible(true);
	}
}
*/
