import React, {useEffect, useState} from "react";
import Header from "../components/Header";
import Result from "../components/Result";
import ReactPaginate from "react-paginate";
import "./Results.css";

const Results = () => {
    const [isLoading, setIsLoading] = useState(true);
    const [searchResults, setSearchResults] = useState([]);
    const [searchTime, setSearchTime] = useState(0);
    const [query, setResult] = React.useState("");
    const [pageNumber, setPageNumber] = useState(0);

    const usersPerPage = 10;
    const pagesVisited = pageNumber * usersPerPage;
    const changePage = ({selected}) => {
        setPageNumber(selected);
    };
    const pageCount = Math.ceil(searchResults.length / usersPerPage);
    const displayUsers = searchResults
        .slice(pagesVisited, pagesVisited + usersPerPage)
        .map((result) => {
            return (
                <Result
                    query={query}
                    url={result.url}
                    title={result.title}
                    occurence={result.occurence}
                />
            );
        });

    useEffect(() => {
        var params = new URLSearchParams(window.location.search);

        // Check if the "query" parameter exists
        if (params.has("query")) {
            // Get the value of the "query" parameter
            var queryString = params.get("query");
            setResult(queryString);
            const isPhrase = queryString.startsWith('"') && queryString.endsWith('"');
            // Use the query parameter value as needed
            console.log("Query parameter value:", queryString);
            const fetchResults = async () => {
                const result = await fetch(
                    `${process.env.REACT_APP_API_URL}/upload?search=${queryString}&phrase=${isPhrase}`,
                    {
                        method: "GET",
                        headers: {
                            "Content-Type": "application/json",
                        },
                    }
                )
                    .then((response) => response.json())
                    .catch((error) => console.log(error));
                if (!result) {
                    setIsLoading(false)
                    return
                }
                console.log(result);
                setSearchResults(result.results);
                setSearchTime(result.time);
                setIsLoading(false);
            };
            fetchResults();
        } else {
            console.log("Query parameter not found in the URL");
        }
    }, []);

    return (
        <>
            <Header query={query}/>
            <div className="pb-20 bg-slate-900">
                {isLoading ? (
                    <h2 className="justify-center px-48 py-8  text-gray-500 ">
                        Loading...
                    </h2>
                ) : (
                    <>
                        <h2 className=" px-48 py-8  text-gray-500">
                            About {searchResults.length} results ({searchTime} seconds){" "}
                        </h2>
                        <div className=" px-48  mb-12">{displayUsers}</div>
                        <ReactPaginate
                            previousLabel={"Previous"}
                            nextLabel={"Next"}
                            pageCount={pageCount}
                            onPageChange={changePage}
                            containerClassName={"paginationBttns"}
                            previousLinkClassName={"previousBttn"}
                            nextLinkClassName={"nextBttn"}
                            disabledClassName={"paginationDisabled"}
                            activeClassName={"paginationActive"}
                        />
                    </>
                )}
                <div className=" "></div>
            </div>
        </>
    );
};

export default Results;
