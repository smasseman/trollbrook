package se.trollbrook.bryggmester.web;

/**
 * @author jorgen.smas@entercash.com
 */
public class RecipeNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -8138117768822198224L;

	private Long id;

	public RecipeNotFoundException(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}
}
