package com.example.springmarket.controller.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.example.springmarket.model.auction.AuctionDTO;
import com.example.springmarket.model.chat.Chat;
import com.example.springmarket.model.chat.ChatDAO;
import com.example.springmarket.model.chat.Room;
import com.example.springmarket.model.chat.RoomDAO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ChatController {

	@Autowired
	RoomDAO roomDao;

	@Autowired
	ChatDAO chatDao;

	List<Room> roomList = new ArrayList<Room>();
	static int roomNumber = 0;

	@RequestMapping("chat/room.do")
	public ModelAndView room(HttpServletRequest request) {

		String url = "";
		HttpSession session = request.getSession();
		String userid = (String) session.getAttribute("userid");

		List<Room> list = roomDao.chatbox(userid);
		url = "chat/box";
		System.out.println(list);
		return new ModelAndView(url, "list", list);
	}

	@RequestMapping("chat/moveChating")
	public ModelAndView chating(@RequestParam HashMap<Object, Object> params) {
		ModelAndView mv = new ModelAndView();
		int roomNumber = Integer.parseInt((String) params.get("roomNumber"));

		mv.addObject("roomNumber", params.get("roomNumber"));
		mv.setViewName("chat/chat");

		return mv;
	}

	@RequestMapping("chat/createchatbox.do")
	public ModelAndView chatbox(@RequestParam(name = "userid") String userid,
			@RequestParam(name = "otherid") String otherid) {
		String url = "";

		int check = roomDao.chatboxcheck(userid, otherid);
		if (check == 1) {
			List<Room> list = roomDao.chatbox(userid);
			url = "chat/box";
			System.out.println(list);
			return new ModelAndView(url, "list", list);

		} else {
			roomDao.craetechatbox(userid, otherid);
			List<Room> list = roomDao.chatbox(userid);
			url = "chat/box";
			System.out.println(list);
			return new ModelAndView(url, "list", list);

		}

	}

	@ResponseBody
	@RequestMapping("chat/savechat.do")
	public String savechat(@RequestParam(name = "roomnumber") String roomnumber,
			@RequestParam(name = "userid") String userid, @RequestParam(name = "message") String message) {
		int roomnumber2 = Integer.parseInt(roomnumber);
		Chat chat = new Chat();
		chat.setRoomnumber(roomnumber2);
		chat.setUserid(userid);
		chat.setMessage(message);
		chatDao.savechat(chat);
		return "redirect:/chat/moveChating?roomNumber=" + roomnumber2;

	}

	@ResponseBody
	@GetMapping("/chat/loadchat.do")
	public ResponseEntity<List<Chat>> loadchat(@RequestParam(name = "roomnumber") String roomnumber) {
		int roomnumber2 = Integer.parseInt(roomnumber);
		List<Chat> chat = chatDao.loadchat(roomnumber2);

		return new ResponseEntity<>(chat, HttpStatus.OK);
	}

}
