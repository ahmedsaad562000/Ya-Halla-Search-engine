import React from "react";

const Result = ({ query, occurence, url, title }) => {
  const text = occurence;
  // Create a regex pattern by joining the words to highlight with '|'
  const wordsArray = text.split(" ");

  // Split the text into an array of substrings
  //   const parts = text.split(pattern);
  console.log("wordsArray", wordsArray);
  console.log("query", query);

  return (
    <div
      id="alert-additional-content-5"
      class="my-3 p-4  rounded-lg bg-slate-800 h-full"
      role="alert"
      style={{ width: "800px" }}
    >
      <div class="flex items-left flex-col">
        <a
          href={`//${url}`}
          class="text-2xl font-medium text-blue-600 dark:text-blue-500 hover:underline"
        >
          {title}{" "}
        </a>
        <h3 class="text-sm text-gray-500">{url}</h3>

        {/* <a class="text-xl font-normal text-gray-800">Computer vision</a> */}
      </div>
      <div class="mt-2 mb-4 text-md text-gray-400 ">
        {wordsArray.map((part, index) =>
          // Wrap the matched words in <strong> tags
          part == query ? <strong>{part} </strong> : part + " "
        )}
      </div>
    </div>
  );
};

export default Result;
