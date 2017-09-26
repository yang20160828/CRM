package com.yang.util;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.util.Assert;

public class HibernatePage<T> implements Serializable {
	private static final long serialVersionUID = 1913490847094219152L;
	private List<T> elements;
	private int pageSize;
	private int pageNumber;
	private long totalElements = 0;

	public HibernatePage(String resultHql, Session session, boolean cacheable,
			int pageNumber, int pageSize, Object... values) {
		Query resultQuery = session.createQuery(resultHql);
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		try {
			String countHql = "select count(*) "
					+ removeSelect(removeOrders(resultQuery.getQueryString()));
			Query countQuery = session.createQuery(countHql);
			if (cacheable) {
				countQuery.setCacheable(true);
				resultQuery.setCacheable(true);
			}
			if(values!=null)
			for (int i = 0; i < values.length; i++) {
				countQuery.setParameter(i, values[i]);
				resultQuery.setParameter(i, values[i]);
			}
			this.totalElements = (Long) countQuery.uniqueResult();
			if (Integer.MAX_VALUE == this.pageNumber
					|| this.pageNumber > getLastPageNumber()) // last page
			{
				this.pageNumber = getLastPageNumber();
			}
			if (this.pageNumber < 1) {
				this.pageNumber = 1;
			}
			elements = resultQuery.setFirstResult(
					(this.pageNumber - 1) * this.pageSize).setMaxResults(
					this.pageSize).list();
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		}
	}

	public HibernatePage(Query resultQuery, Session session, boolean cacheable,
			int pageNumber, int pageSize, Object... values) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		try {
			String countHql = "select count(*) "
					+ removeSelect(removeOrders(resultQuery.getQueryString()));
			Query countQuery = session.createQuery(countHql);
			if (cacheable) {
				countQuery.setCacheable(true);
				resultQuery.setCacheable(true);
			}
			for (int i = 0; i < values.length; i++) {
				countQuery.setParameter(i, values[i]);
				resultQuery.setParameter(i, values[i]);
			}
			this.totalElements = (Long) countQuery.uniqueResult();
			if (Integer.MAX_VALUE == this.pageNumber
					|| this.pageNumber > getLastPageNumber()) // last page
			{
				this.pageNumber = getLastPageNumber();
			}
			if (this.pageNumber < 1) {
				this.pageNumber = 1;
			}
			elements = resultQuery.setFirstResult(
					(this.pageNumber - 1) * this.pageSize).setMaxResults(
					this.pageSize).list();
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 构建HibernatePage对象，完成Hibernate的Query数据的分页处理
	 * 
	 * @param resultQuery
	 *            Hibernate的Query对象
	 * @param total
	 *            统计总数
	 * @param pageNumber
	 *            当前页编码，从1开始，如果传的值为Integer.MAX_VALUE表示获取最后一页。
	 *            如果你不知道最后一页编码，传Integer.MAX_VALUE即可。如果当前页超过总页数，也表示最后一页。
	 *            这两种情况将重新更改当前页的页码，为最后一页编码。
	 * @param pageSize
	 *            每一页显示的条目数
	 */
	public HibernatePage(Query resultQuery, int total, int pageNumber,
			int pageSize) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		try {
			this.totalElements = total;
			if (Integer.MAX_VALUE == this.pageNumber
					|| this.pageNumber > getLastPageNumber()) // last page
			{
				this.pageNumber = getLastPageNumber();
			}
			if (this.pageNumber < 1) {
				this.pageNumber = 1;
			}

			elements = resultQuery.setFirstResult(
					(this.pageNumber - 1) * this.pageSize).setMaxResults(
					this.pageSize).list();
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 构建HibernatePage对象，完成Hibernate的分页处理
	 * 
	 * @param elements
	 *            当前页面要显示的数据
	 * @param total
	 *            所有页面数据总数
	 * @param pageNumber
	 *            当前页编码，从1开始，如果传的值为Integer.MAX_VALUE表示获取最后一页。
	 *            如果你不知道最后一页编码，传Integer.MAX_VALUE即可。如果当前页超过总页数，也表示最后一页。
	 *            这两种情况将重新更改当前页的页码，为最后一页编码。
	 * @param pageSize
	 *            每一页显示的条目数
	 */
	public HibernatePage(List elements, int total, int pageNumber, int pageSize) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.totalElements = total;

		if (Integer.MAX_VALUE == this.pageNumber
				|| this.pageNumber > getLastPageNumber()) // last page
		{
			this.pageNumber = getLastPageNumber();
		}
		if (this.pageNumber < 1) {
			this.pageNumber = 1;
		}

		if ((int) getThisPageLastElementNumber() > getThisPageFirstElementNumber()
				&& elements.size() >= (int) getThisPageLastElementNumber()) {
			this.elements = elements.subList(
					getThisPageFirstElementNumber() - 1,
					(int) getThisPageLastElementNumber());
		} else {
			this.elements = elements;
		}

	}

	public boolean isFirstPage() {
		return getPageNumber() == 1;
	}

	public boolean isLastPage() {
		return getPageNumber() >= getLastPageNumber();
	}

	public boolean hasNextPage() {
		return getLastPageNumber() > getPageNumber();
	}

	public boolean hasPreviousPage() {
		return getPageNumber() > 1;
	}

	public int getLastPageNumber() {
		return (int) (totalElements % this.pageSize == 0 ? totalElements
				/ this.pageSize : totalElements / this.pageSize + 1);
	}

	/**
	 * 返回List类型数据
	 * 
	 * @return List数据源
	 */
	public List<T> getElements() {
		return elements;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public int getThisPageFirstElementNumber() {
		return (getPageNumber() - 1) * getPageSize() + 1;
	}

	public long getThisPageLastElementNumber() {
		int fullPage = getThisPageFirstElementNumber() + getPageSize() - 1;
		return getTotalElements() < fullPage ? getTotalElements() : fullPage;
	}

	public int getNextPageNumber() {
		return getPageNumber() + 1;
	}

	public int getPreviousPageNumber() {
		return getPageNumber() - 1;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	/*
	 * 去除select子句，未考虑union的情况
	 */
	private static String removeSelect(String hql) {
		Assert.hasText(hql);
		int beginPos = hql.toLowerCase().indexOf("from");
		Assert.isTrue(beginPos != -1, " hql : " + hql
				+ " must has a keyword 'from'");
		return hql.substring(beginPos);
	}

	/*
	 * 去除orderby 子句
	 */
	private static String removeOrders(String hql) {
		Assert.hasText(hql);
		Pattern p = Pattern.compile("order\\s*by[\\w|\\W|\\s|\\S]*",
				Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(hql);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "");
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
