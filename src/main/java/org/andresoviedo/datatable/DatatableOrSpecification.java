package org.andresoviedo.datatable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.andresoviedo.datatable.dto.DatatableColumn;
import org.andresoviedo.datatable.dto.DatatableSearch;

/**
 * Construction d'un predicat "or"
 * 
 * @author afoviedo
 * @param <T>
 *            type de l'entité
 */
class DatatableOrSpecification<T> implements Specification<T> {

	private Logger LOGGER = Logger.getLogger("");

    private final List<DatatableColumn> colonnes;
    private final DatatableSearch search;

    /**
     * Constructor de predicat "or"
     * 
     * @param colonnes
     *            liste de colonnes
     * @param search
     *            texte a chercher
     */
    DatatableOrSpecification(final List<DatatableColumn> colonnes, final DatatableSearch search) {
        this.colonnes = colonnes;
        this.search = search;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {

    	LOGGER.log(Level.FINE, "Creating OR search predicate: '{}':'{}'", new Object[]{search, colonnes});

        // liste de predicats pour aprés faire le "or"
        final List<Predicate> predicates = new ArrayList<Predicate>();

        // valeur a chercher
        final String valeurRecherche = search.getValue().trim().toLowerCase();

        for (final DatatableColumn colonne : colonnes) {
            final Path<?> expression = DatatableHelper.getExpression(root, colonne.getData(), null);
            if (expression.getJavaType() == String.class) {
                String valuerFinale = valeurRecherche;
                if (colonne.isSearchWithoutSpaces()) {
                    // le siret par exemple no contient pas des espaces
                    valuerFinale = valeurRecherche.replace(" ", "");
                }
                predicates.add(cb.like(cb.lower(expression.as(String.class)),
                    DatatableSpecification.getLikeFilterValue(valuerFinale),
                    DatatableSpecification.ESCAPE_CHAR));
            } else if (expression.getJavaType() == Integer.class) {
                predicates.add(cb.like(expression.as(String.class),
                    DatatableSpecification.getLikeFilterValue(valeurRecherche),
                    DatatableSpecification.ESCAPE_CHAR));
            } else {
                throw new IllegalArgumentException("La colonne n'a pas le type supporté [String.class|Integer.class] ("
                    + expression.getJavaType() + ")");
            }
        }

        return cb.or(predicates.toArray(new Predicate[predicates.size()]));
    }
}
