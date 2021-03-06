package com.wust.boyaBookStore.controller;


import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.ImageIcon;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wust.boyaBookStore.po.Book;
import com.wust.boyaBookStore.po.Category;
import com.wust.boyaBookStore.po.PageBean;
import com.wust.boyaBookStore.service.BookService;
import com.wust.boyaBookStore.service.CategoryService;

import cn.itcast.commons.CommonUtils;
@Controller
public class AdminBookController {
    @Autowired
    private BookService bookService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 删除图书
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/AdminBookServlet")
    public String operator(HttpServletRequest req, String bid,HttpServletResponse resp, Book book ,Category category) throws Exception, IOException{
        String string = req.getParameter("method");
        if (string.equals("delete")) {
            return delete(req, bid, resp);
        }else{
            return edit(req, book, category);
        }
    }
    
    public String delete(HttpServletRequest req, String bid,HttpServletResponse resp) throws ServletException, IOException {
 
        /*
         * 删除图片
         */
        Book book = null;
        try {
            book = bookService.load(bid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String savepath = req.getServletContext().getRealPath("/");// 获取真实的路径
        new File(savepath, book.getImageW()).delete();// 删除文件
        new File(savepath, book.getImageB()).delete();// 删除文件

        bookService.delete(bid);// 删除数据库的记录

        req.setAttribute("msg", "删除图书成功！");
        return "adminjsps/msg";
    }

    /**
     * 修改图书
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    public String edit(HttpServletRequest req,  Book book ,Category category) throws ServletException, IOException {
        book.setCategory(category);
        bookService.edit(book);
        req.setAttribute("msg", "修改图书成功！");
        return "adminjsps/msg";
    }

    /**
     * 加载图书
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/loadbookadmin")
    public String load(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*
         * 1. 获取bid，得到Book对象，保存之
         */
        String bid = req.getParameter("bid");
        Book book = null;
        try {
            book = bookService.load(bid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        req.setAttribute("book", book);

        /*
         * 2. 获取所有一级分类，保存之
         */
        req.setAttribute("parents", categoryService.findParents());
        /*
         * 3. 获取当前图书所属的一级分类下所有2级分类
         */
        String pid = book.getCategory().getPid();
        req.setAttribute("children", categoryService.findChildren(pid));

        /*
         * 4. 转发到desc显示
         */
        return "adminjsps/admin/book/desc";
    }

    /**
     * 添加图书：第一步
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/addPre")
    public String addPre(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*
         * 1. 获取所有一级分类，保存之 2. 转发到add，该页面会在下拉列表中显示所有一级分类
         */
        List<Category> parents = categoryService.findParents();
        req.setAttribute("parents", parents);
        return "adminjsps/admin/book/add";
    }
     @RequestMapping("/ajaxFindChildren")
    public  @ResponseBody List<Category>  ajaxFindChildren(HttpServletRequest req, String pid )
            throws ServletException, IOException {
        /*
         * 1. 获取pid 2. 通过pid查询出所有2级分类 3. 把List<Category>转换成json，输出给客户端
         */
        List<Category> children = categoryService.findChildren(pid);
        return children;
    }

   
    /**
     * 显示所有分类
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/findCateforyAlladmin")
    public String findCategoryAll(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        /*
         * 1. 通过service得到所有的分类 2. 保存到request中，转发到left
         */
        List<Category> parents = null;
        try {
            parents = categoryService.findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        req.setAttribute("parents", parents);
        return "adminjsps/admin/book/left";
    }

    /**
     * 获取当前页码
     * 
     * @param req
     * @return
     */
    private int getPc(HttpServletRequest req) {
        int pc = 1;
        String param = req.getParameter("pc");
        if (param != null && !param.trim().isEmpty()) {
            try {
                pc = Integer.parseInt(param);
            } catch (RuntimeException e) {
            }
        }
        return pc;
    }

    /**
     * 截取url，页面中的分页导航中需要使用它做为超链接的目标！
     * 
     * @param req
     * @return
     */
    /*
     * http://localhost:8080/goods/BookServlet?methed=findByCategory&cid=xxx&pc=3 /goods/BookServlet +
     * methed=findByCategory&cid=xxx&pc=3
     */
    private String getUrl(HttpServletRequest req) {
        String url = req.getRequestURI() + "?" + req.getQueryString();
        /*
         * 如果url中存在pc参数，截取掉，如果不存在那就不用截取。
         */
        int index = url.lastIndexOf("&pc=");
        if (index != -1) {
            url = url.substring(0, index);
        }
        return url;
    }

    /**
     * 按分类查
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/findByCategoryAdmin")
    public String findByCategory(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        /*
         * 1. 得到pc：如果页面传递，使用页面的，如果没传，pc=1
         */
        int pc = getPc(req);
        /*
         * 2. 得到url：...
         */
        String url = getUrl(req);
        /*
         * 3. 获取查询条件，本方法就是cid，即分类的id
         */
        String cid = req.getParameter("cid");
        /*
         * 4. 使用pc和cid调用service#findByCategory得到PageBean
         */
        PageBean<Book> pb = bookService.findByCategory(cid, pc);
        /*
         * 5. 给PageBean设置url，保存PageBean，转发到/jsps/book/list
         */
        pb.setUrl(url);
        req.setAttribute("pb", pb);
        return "adminjsps/admin/book/list";
    }

    /**
     * 按作者查
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/findByAuthorAdmin")
    public String findByAuthor(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*
         * 1. 得到pc：如果页面传递，使用页面的，如果没传，pc=1
         */
        int pc = getPc(req);
        /*
         * 2. 得到url：...
         */
        String url = getUrl(req);
        /*
         * 3. 获取查询条件，本方法就是cid，即分类的id
         */
        String author = req.getParameter("author");
        /*
         * 4. 使用pc和cid调用service#findByCategory得到PageBean
         */
        PageBean<Book> pb = bookService.findByAuthor(author, pc);
        /*
         * 5. 给PageBean设置url，保存PageBean，转发到/jsps/book/list
         */
        pb.setUrl(url);
        req.setAttribute("pb", pb);
        return "adminjsps/admin/book/list";
    }

    /**
     * 按出版社查询
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/findByPressAdmin")
    public String findByPress(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*
         * 1. 得到pc：如果页面传递，使用页面的，如果没传，pc=1
         */
        int pc = getPc(req);
        /*
         * 2. 得到url：...
         */
        String url = getUrl(req);
        /*
         * 3. 获取查询条件，本方法就是cid，即分类的id
         */
        String press = req.getParameter("press");
        /*
         * 4. 使用pc和cid调用service#findByCategory得到PageBean
         */
        PageBean<Book> pb = bookService.findByPress(press, pc);
        /*
         * 5. 给PageBean设置url，保存PageBean，转发到/jsps/book/list
         */
        pb.setUrl(url);
        req.setAttribute("pb", pb);
        return "adminjsps/admin/book/list";
    }

    /**
     * 按图名查
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/findByNameAdmin")
    public String findByBname(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*
         * 1. 得到pc：如果页面传递，使用页面的，如果没传，pc=1
         */
        int pc = getPc(req);
        /*
         * 2. 得到url：...
         */
        String url = getUrl(req);
        /*
         * 3. 获取查询条件，本方法就是cid，即分类的id
         */
        String bname = req.getParameter("bname");
        /*
         * 4. 使用pc和cid调用service#findByCategory得到PageBean
         */
        PageBean<Book> pb = bookService.findByBname(bname, pc);
        /*
         * 5. 给PageBean设置url，保存PageBean，转发到/jsps/book/list
         */
        pb.setUrl(url);
        req.setAttribute("pb", pb);
        return "adminjsps/admin/book/list";
    }

    /**
     * 多条件组合查询
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/findByCombinationAdmin")
    public String findByCombination(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        /*
         * 1. 得到pc：如果页面传递，使用页面的，如果没传，pc=1
         */
        int pc = getPc(req);
        /*
         * 2. 得到url：...
         */
        String url = getUrl(req);
        /*
         * 3. 获取查询条件，本方法就是cid，即分类的id
         */
        Book criteria = CommonUtils.toBean(req.getParameterMap(), Book.class);
        /*
         * 4. 使用pc和cid调用service#findByCategory得到PageBean
         */
        PageBean<Book> pb = bookService.findByCombination(criteria, pc);
        /*
         * 5. 给PageBean设置url，保存PageBean，转发到/jsps/book/list
         */
        pb.setUrl(url);
        req.setAttribute("pb", pb);
        return "adminjsps/admin/book/list";
    }
    @RequestMapping("/AdminAddBook")
    public void AdminAddBook(HttpServletRequest request, HttpServletResponse response) throws Exception{
        /*
         * 1. commons-fileupload的上传三步
         */
        // 创建工具
        FileItemFactory factory = new DiskFileItemFactory();
        /*
         * 2. 创建解析器对象
         */
        ServletFileUpload sfu = new ServletFileUpload(factory);
        sfu.setFileSizeMax(80 * 1024);//设置单个上传的文件上限为80KB
        /*
         * 3. 解析request得到List<FileItem>
         */
        List<FileItem> fileItemList = null;
        try {
            fileItemList = sfu.parseRequest(request);
        } catch (FileUploadException e) {
            // 如果出现这个异步，说明单个文件超出了80KB
            error("上传的文件超出了80KB", request, response);
            return;
        }
        
        /*
         * 4. 把List<FileItem>封装到Book对象中
         * 4.1 首先把“普通表单字段”放到一个Map中，再把Map转换成Book和Category对象，再建立两者的关系
         */
        Map<String,Object> map = new HashMap<String,Object>();
        for(FileItem fileItem : fileItemList) {
            if(fileItem.isFormField()) {//如果是普通表单字段
                map.put(fileItem.getFieldName(), fileItem.getString("UTF-8"));
            }
        }
        Book book = CommonUtils.toBean(map, Book.class);//把Map中大部分数据封装到Book对象中
        Category category = CommonUtils.toBean(map, Category.class);//把Map中cid封装到Category中
        book.setCategory(category);
        
        /*
         * 4.2 把上传的图片保存起来
         *   > 获取文件名：截取之
         *   > 给文件添加前缀：使用uuid前缀，为也避免文件同名现象
         *   > 校验文件的扩展名：只能是jpg
         *   > 校验图片的尺寸
         *   > 指定图片的保存路径，这需要使用ServletContext#getRealPath()
         *   > 保存之
         *   > 把图片的路径设置给Book对象
         */
        // 获取文件名
        FileItem fileItem = fileItemList.get(1);//获取大图
        String filename = fileItem.getName();
        // 截取文件名，因为部分浏览器上传的绝对路径
        int index = filename.lastIndexOf("\\");
        if(index != -1) {
            filename = filename.substring(index + 1);
        }
        // 给文件名添加uuid前缀，避免文件同名现象
        filename = CommonUtils.uuid() + "_" + filename;
        // 校验文件名称的扩展名
        if(!filename.toLowerCase().endsWith(".jpg")) {
            try {
                error("上传的图片扩展名必须是JPG", request, response);
            } catch (ServletException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }
        // 校验图片的尺寸
        // 保存上传的图片，把图片new成图片对象：Image、Icon、ImageIcon、BufferedImage、ImageIO
        /*
         * 保存图片：
         * 1. 获取真实路径
         */
        String savepath = request.getServletContext().getRealPath("/book_img");
        /*
         * 2. 创建目标文件
         */
        File destFile = new File(savepath, filename);
        /*
         * 3. 保存文件
         */
        try {
            fileItem.write(destFile);//它会把临时文件重定向到指定的路径，再删除临时文件
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 校验尺寸
        // 1. 使用文件路径创建ImageIcon
        ImageIcon icon = new ImageIcon(destFile.getAbsolutePath());
        // 2. 通过ImageIcon得到Image对象
        Image image = icon.getImage();
        // 3. 获取宽高来进行校验
        if(image.getWidth(null) > 350 || image.getHeight(null) > 350) {
            try {
                error("您上传的图片尺寸超出了350*350！", request, response);
            } catch (ServletException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            destFile.delete();//删除图片
            return;
        }
        
        // 把图片的路径设置给book对象
        book.setImageW("book_img/" + filename);
        
        


        // 获取文件名
        fileItem = fileItemList.get(2);//获取小图
        filename = fileItem.getName();
        // 截取文件名，因为部分浏览器上传的绝对路径
        index = filename.lastIndexOf("\\");
        if(index != -1) {
            filename = filename.substring(index + 1);
        }
        // 给文件名添加uuid前缀，避免文件同名现象
        filename = CommonUtils.uuid() + "_" + filename;
        // 校验文件名称的扩展名
        if(!filename.toLowerCase().endsWith(".jpg")) {
            try {
                error("上传的图片扩展名必须是JPG", request, response);
            } catch (ServletException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }
        // 校验图片的尺寸
        // 保存上传的图片，把图片new成图片对象：Image、Icon、ImageIcon、BufferedImage、ImageIO
        /*
         * 保存图片：
         * 1. 获取真实路径
         */
        savepath = request.getServletContext().getRealPath("/book_img");
        /*
         * 2. 创建目标文件
         */
        destFile = new File(savepath, filename);
        /*
         * 3. 保存文件
         */
        try {
            fileItem.write(destFile);//它会把临时文件重定向到指定的路径，再删除临时文件
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 校验尺寸
        // 1. 使用文件路径创建ImageIcon
        icon = new ImageIcon(destFile.getAbsolutePath());
        // 2. 通过ImageIcon得到Image对象
        image = icon.getImage();
        // 3. 获取宽高来进行校验
        if(image.getWidth(null) > 350 || image.getHeight(null) > 350) {
            try {
                error("您上传的图片尺寸超出了350*350！", request, response);
            } catch (ServletException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            destFile.delete();//删除图片
            return;
        }
        
        // 把图片的路径设置给book对象
        book.setImageB("book_img/" + filename);
        
        
        
        
        // 调用service完成保存
        book.setBid(CommonUtils.uuid());
        bookService.add(book);
        
        // 保存成功信息转发到msg
        request.setAttribute("msg", "添加图书成功！");
        try {
            request.getRequestDispatcher("/adminjsps/msg.jsp").forward(request, response);
        } catch (ServletException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /*
     * 保存错误信息，转发到add
     */
    private void error(String msg, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("msg", msg);
        request.setAttribute("parents", categoryService.findParents());//所有一级分类
        request.getRequestDispatcher("/adminjsps/admin/book/add").
                forward(request, response);
    }
}
