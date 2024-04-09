package com.example.springmarket.controller.product;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.example.springmarket.model.product.PageUtil;
import com.example.springmarket.model.product.ProductDAO;
import com.example.springmarket.model.product.ProductDTO;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/product/*")
public class ProductController {

	@Autowired
	ProductDAO productDao;

	// 가지마켓 상품 리스트
	@GetMapping("list")
	public ModelAndView list(ModelAndView mav, @RequestParam(name = "cur_page", defaultValue = "1") int curPage,
			@RequestParam(name = "count", defaultValue = "1") int count) {
//         int count = productDao.count();
		if (curPage < 1) {
			curPage = 1;
		}
		PageUtil page = new PageUtil(count, curPage);

		int start = page.getPageBegin();
		int end = page.getPageEnd();

		List<ProductDTO> list = productDao.list(start, end);
		mav.setViewName("product/list");
		mav.addObject("page", page);
		mav.addObject("list", list);

		return mav;
	}

	// 상품추가하는는 화면으로 이동
	@GetMapping("write")
	public String write() {
		return "product/write";
	}

	// 상품 추가
	@PostMapping("insert")
	public String insert(ProductDTO dto, HttpServletRequest request, HttpSession session) {

		String filename = "-";
		MultipartFile file = dto.getFile(); // 파일 객체 가져오기
		String userid = (String) session.getAttribute("userid");

		// 파일이 비어있지 않고, 파일의 원본 이름이 null이 아닌 경우에만 처리
		if (file != null && !file.isEmpty() && file.getOriginalFilename() != null) {
			filename = file.getOriginalFilename(); // 파일명 설정

			try {
				ServletContext application = request.getSession().getServletContext();
				String path = application.getRealPath("/resources/images/");
				File directory = new File(path);

				if (!directory.exists()) {
					directory.mkdirs(); // 디렉토리가 없으면 생성
				}

				// 파일 저장
				file.transferTo(new File(directory, filename));
			} catch (Exception e) {
				e.printStackTrace();
				// 파일 저장 중 오류 발생 시, 적절한 예외 처리 로직 추가 가능
			}
		}

		dto.setFilename(filename); // 파일명 설정
		dto.setUserid(userid);

		productDao.insert(dto); // 데이터베이스에 저장
		return "redirect:/product/list";
	}

	// 상품 클릭시 디테일
	@GetMapping("detail/{write_code}")
	public ModelAndView detail(@PathVariable(name = "write_code") int write_code, ModelAndView mav,HttpSession session) {
		String userid =(String)session.getAttribute("userid");
		int check = productDao.loveCheck(userid, write_code);
		session.setAttribute("check", check);
		mav.setViewName("product/detail");
		mav.addObject("dto", productDao.detail(write_code));
		return mav;
	}

	// 상품 삭제
	@GetMapping("delete")
	public String delete(@RequestParam(name = "write_code") int write_code, HttpServletRequest request) {
		String filename = productDao.file_info(write_code);
		if (filename != null && !filename.equals("-")) {
			ServletContext application = request.getSession().getServletContext();
			String path = application.getRealPath("/resources/images/");
			File f = new File(path + filename);
			if (f.exists())
				f.delete();
		}
		System.out.println(write_code);
		productDao.delete(write_code);
		return "redirect:/product/list";
	}

	// 추가한 상품을 내정보에서 확인
	@GetMapping("mylist")
	public ModelAndView mylist(HttpSession session, ModelAndView mav) {
		String userid = (String) session.getAttribute("userid");
		mav.setViewName("/product/mylist");
		System.out.println(productDao.mylist(userid));
		mav.addObject("list", productDao.mylist(userid));
		return mav;
	}

	// 내 판매내역 삭제
	@GetMapping("mylist_delete")
	public String mylist_delete(@RequestParam(name = "write_code") int write_code, HttpServletRequest request) {
		String filename = productDao.file_info(write_code);
		if (filename != null && !filename.equals("-")) {
			ServletContext application = request.getSession().getServletContext();
			String path = application.getRealPath("/resources/images/");
			File f = new File(path + filename);
			if (f.exists())
				f.delete();
		}
		System.out.println(write_code);
		productDao.delete(write_code);
		return "redirect:/product/mylist";
	}

	// 내 판매내역에서 상태 변경
	@GetMapping("updateStatus")
	public ModelAndView update_Status(@RequestParam("write_code") int writeCode,
			@RequestParam("status_code") int statusCode) {

		ProductDTO dto = new ProductDTO();
		dto.setWrite_code(writeCode);
		dto.setStatus_code(statusCode);

		// 유저 아이디와 DTO를 이용하여 상태 업데이트 수행
		productDao.updateStatus(dto);
		;

		// 리스트 페이지로 리다이렉트
		return new ModelAndView("redirect:/product/mylist");
	}

	// 내 판매내역의 편집으로 이동
	@GetMapping("edit/{write_code}")
	public ModelAndView edit_Mylist(@PathVariable(name = "write_code") int write_code, ModelAndView mav) {
		mav.setViewName("/product/edit");
		mav.addObject("dto", productDao.detail(write_code));
		return mav;
	}

	// 내 판매내역에서 상품 수정
	@PostMapping("update")
	public String update(ProductDTO dto, HttpServletRequest request) {
		String filename = "-";
		
		if (!dto.getFile().isEmpty()) {
			filename = dto.getFile().getOriginalFilename();
			try {
				ServletContext application = request.getSession().getServletContext();
				String path = application.getRealPath("/resources/images/");
				new File(path).mkdir();
				dto.getFile().transferTo(new File(path + filename));
			} catch (Exception e) {
				e.printStackTrace();
			}
			dto.setFilename(filename);
		} else {
			ProductDTO dto2 = productDao.detail(dto.getWrite_code());
			dto.setFilename(dto2.getFilename());
		}
		productDao.update(dto);
		return "redirect:/product/mylist";
	}

	// 상품 검색

}