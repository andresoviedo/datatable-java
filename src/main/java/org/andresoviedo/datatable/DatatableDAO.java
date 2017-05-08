package org.andresoviedo.datatable;

import org.andresoviedo.datatable.dto.DatatableQuery;
import org.andresoviedo.datatable.dto.DatatableResult;

/**
 * Interface pour les implementations du Datatable server side
 * 
 * @param <T>
 *            type de l'entité JPA
 * @author afoviedo
 */
public interface DatatableDAO<T> {

    /**
     * Returns the filtered list for the given {@link DatatableQuery}.
     * 
     * @param input
     *            the {@link DatatableQuery} mapped from the Ajax request
     * @param <R>
     *            type du retour de la requete
     * @return a {@link DatatableResult}
     */
    <R> DatatableResult<R> findAll(DatatableQuery<R, T> input);

    /**
     * Returns the filtered list for the given {@link DatatableQuery}.
     * 
     * @param input
     *            the {@link DatatableQuery} mapped from the Ajax request
     * @param additionalSpecification
     *            an additional {@link Specification} to apply to the query (with an "AND" clause)
     * @param <R>
     *            type du retour de la requete
     * @return a {@link DatatableResult}
     */
    <R> DatatableResult<R> findAll(DatatableQuery<R, T> input, Specification<T> additionalSpecification);

    /**
     * Returns the filtered list for the given {@link DatatableQuery}.
     * 
     * @param input
     *            the {@link DatatableQuery} mapped from the Ajax request
     * @param additionalSpecification
     *            an additional {@link Specification} to apply to the query (with an "AND" clause)
     * @param preFilteringSpecification
     *            a pre-filtering {@link Specification} to apply to the query (with an "AND" clause)
     * @param <R>
     *            type du retour de la requete
     * @return a {@link DatatableResult}
     */
    <R> DatatableResult<R> findAll(DatatableQuery<R, T> input, Specification<T> additionalSpecification,
        Specification<T> preFilteringSpecification);
}
