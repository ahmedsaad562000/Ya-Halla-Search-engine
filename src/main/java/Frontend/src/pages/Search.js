import React from "react";
import logo from "../HALLA.png";

const Search = () => {
  const handleClick = (e) => {
    e.preventDefault();
    const query = document.getElementById("query").value;

    if (query) {
      window.location.href = `/results?query=${query}`;
    } else {
    }
  };
  return (
    // <div className=" bg-gray-50">

    <section class="  bg-gradient-to-r from-slate-800  to-slate-900  pt-48 pb-48 h-full h-screen">
      <div class="flex flex-col items-center justify-center  px-6 py-24 mx-auto h-100 ">
        <img class="h-24 mb-12  mr-2" src={logo} alt="logo" />
        <div>
          {/* <input style={{width: "700px"}} type="text" name="query" id="query" class="w-ful bg-gray-50 border-2  border-gray-300 text-gray-900 sm:text-md rounded-2xl focus:ring-none focus:ring-0 focus:ring-offset-0  p-2.5  " placeholder="Enter you query" required={true} /> */}
        </div>
        <form>
          <label
            for="default-search"
            class="mb-2 text-sm font-medium text-gray-900 sr-only dark:text-white"
          >
            Search
          </label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
              <svg
                aria-hidden="true"
                class="w-5 h-5 text-gray-500 dark:text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                ></path>
              </svg>
            </div>
            <input
              style={{ width: "700px" }}
              type="text"
              name="query"
              id="query"
              class="block w-full p-4 pl-10 text-sm text-gray-900 border border-gray-300 rounded-2xl bg-gray-50 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
              placeholder="Enter your query"
              required={true}
            />

            {/* <input style={{width: "700px"}} type="text" name="query" id="query" class="w-ful p-4 pl-10 bg-gray-50 border-2  border-gray-300 text-gray-900 sm:text-md rounded-2xl focus:ring-none focus:ring-0 focus:ring-offset-0  " placeholder="Enter your query" required={true} /> */}
          </div>
        </form>
        <button
          onClick={handleClick}
          type="button"
          class="mt-4 text-white bg-gray-800 hover:bg-gray-900 focus:outline-none focus:ring-4 focus:ring-gray-300 font-medium rounded-lg text-sm px-12 py-3 mr-2 mb-2 dark:bg-gray-800 dark:hover:bg-gray-700 dark:focus:ring-gray-700 dark:border-gray-700"
        >
          Yalla
        </button>
      </div>
    </section>
    //  </div>
  );
};

export default Search;
