package se.trollbrook.springboot;

import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import se.trollbrook.spring.web.servlet.interceptors.LogModelAndViewInterceptor;
import se.trollbrook.spring.web.servlet.interceptors.MessagesInterceptor;
import se.trollbrook.spring.web.servlet.interceptors.ModelObjectInserterInterceptor;

@Configuration
@EnableAutoConfiguration
@ComponentScan("se.trollbrook")
public class Application extends WebMvcConfigurerAdapter {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LogModelAndViewInterceptor()).addPathPatterns("/**/*.html");
		registry.addInterceptor(new MessagesInterceptor()).addPathPatterns("/**/*.html");
		registry.addInterceptor(new ModelObjectInserterInterceptor("ctxRoot", "")).addPathPatterns("/**/*.html");
		registry.addInterceptor(new ModelObjectInserterInterceptor("esc", new EscapeTool())).addPathPatterns(
				"/**/*.html");
		registry.addInterceptor(new ModelObjectInserterInterceptor("date", new DateTool())).addPathPatterns(
				"/**/*.html");
	}

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(Application.class, args);
		ctx.toString();
		/*
		String[] beanNames = ctx.getBeanDefinitionNames();
		java.util.Arrays.sort(beanNames);
		for (String beanName : beanNames) {
			System.out.println(beanName);
		}
		System.out.println(ctx.getBean(VelocityEngine.class).getTemplate("index.vm").getEncoding());
		*/
	}
}